package lutukai.pesalens.feature.assistant.impl

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.common.AppError
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.domain.ai.IntentInterpreter
import lutukai.pesalens.core.domain.ai.InterpretFailure
import lutukai.pesalens.core.domain.ai.Interpretation
import lutukai.pesalens.core.domain.repository.FinanceRepository
import lutukai.pesalens.core.domain.repository.SettingsRepository
import lutukai.pesalens.core.model.AffordabilityOutcome
import lutukai.pesalens.core.model.AppSettings
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.BudgetDraft
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.testing.MainDispatcherRule
import lutukai.pesalens.core.testing.SampleFinance
import lutukai.pesalens.core.testing.TestClock
import lutukai.pesalens.core.testing.fake.FakeConversationSession
import lutukai.pesalens.core.testing.fake.FakeFinanceRepository
import lutukai.pesalens.core.testing.fake.FakeIntentInterpreter
import lutukai.pesalens.core.testing.fake.FakeModelManager
import lutukai.pesalens.core.testing.fake.FakeSettingsRepository
import lutukai.pesalens.core.testing.fake.FakeUserMessageSink
import lutukai.pesalens.core.ui.catalog.model.AffordabilityFormUi
import lutukai.pesalens.core.ui.catalog.model.AffordabilityUi
import lutukai.pesalens.core.ui.catalog.model.BudgetStatus
import lutukai.pesalens.core.ui.catalog.model.NoticeKind
import lutukai.pesalens.core.ui.catalog.model.StepDirection
import lutukai.pesalens.core.ui.catalog.model.monthBlocks
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantDialog
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantEffect
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantIntent
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantViewModel
import lutukai.pesalens.feature.assistant.impl.presentation.ChatItem
import lutukai.pesalens.feature.assistant.impl.presentation.NoticeAction
import lutukai.pesalens.feature.assistant.impl.presentation.Prompt
import lutukai.pesalens.feature.assistant.impl.presentation.Suggestion
import lutukai.pesalens.feature.assistant.impl.presentation.ThinkingLabel
import lutukai.pesalens.feature.assistant.impl.presentation.suggestionsAfter
import lutukai.pesalens.feature.assistant.impl.presentation.toUi
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin
import org.junit.Rule
import org.junit.Test

class AssistantViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val november = YearMonth(2026, 11)
    private val interpreter = FakeIntentInterpreter()
    private val finance = FakeFinanceRepository()
    private val settings = FakeSettingsRepository(AppSettings(cloudConsentGiven = true))
    private val models = FakeModelManager()
    private val conversation = FakeConversationSession()
    private val messages = FakeUserMessageSink()
    private val clock = TestClock() // 2026-11-10T09:41Z

    private fun vm(
        interpreter: IntentInterpreter = this.interpreter,
        finance: FinanceRepository = this.finance,
        settings: SettingsRepository = this.settings,
    ) = AssistantViewModel(interpreter, finance, settings, models, conversation, messages, clock, TimeZone.UTC)
        .also { it.onIntent(AssistantIntent.Load) }

    private fun AssistantViewModel.send(text: String) {
        onIntent(AssistantIntent.DraftChanged(text))
        onIntent(AssistantIntent.Send)
    }

    private val AssistantViewModel.items get() = state.value.items

    private inline fun <reified T : ChatItem> AssistantViewModel.last(): T = items.filterIsInstance<T>().last()

    /** Asks about a KSh 90,000 laptop and returns the details card. */
    private fun AssistantViewModel.openDetails(): ChatItem.AffordabilityDetails {
        interpreter.next = Interpretation.Affordability(Money.kes(90_000))
        send("Can I afford a KSh 90,000 laptop?")
        return last()
    }

    /** Submits 30,000 set aside and 10,000 a month against a 90,000 price and returns the result card. */
    private fun AssistantViewModel.openResult(): ChatItem.AffordabilityResult {
        val details = openDetails()
        onIntent(AssistantIntent.UseSampleValues(details.id))
        onIntent(AssistantIntent.SubmitForm(details.id))
        return last()
    }

    private fun AssistantViewModel.openBudget(): ChatItem.Budget {
        interpreter.next = Interpretation.Budget
        send("Help me create a budget")
        return last()
    }

    private fun AssistantViewModel.budget(id: Long) = items.filterIsInstance<ChatItem.Budget>().single { it.id == id }

    private fun AssistantViewModel.result(id: Long) = items.filterIsInstance<ChatItem.AffordabilityResult>().single { it.id == id }

    // ---- Send ----

    @Test
    fun send_blank_doesNothing() = runTest {
        val vm = vm()
        vm.send("   ")
        assertEquals(persistentListOf(), vm.items)
        assertEquals(emptyList(), interpreter.received)
        assertEquals(false, vm.state.value.busy)
    }

    @Test
    fun send_cloudUnreachable_appendsUserThenCloudOfflineNotice() = runTest {
        interpreter.next = Interpretation.Failed(InterpretFailure.CloudUnreachable)
        val vm = vm()
        vm.send("  Where did my money go this month?  ")
        val (user, notice) = vm.items.also { assertEquals(2, it.size) }
        assertEquals(ChatItem.User(user.id, UiText.Raw("Where did my money go this month?")), user)
        assertEquals(ChatItem.Notice(notice.id, AssistantMode.Cloud, NoticeKind.CloudOffline, null), notice)
        assertEquals("", vm.state.value.draft)
        assertEquals(false, vm.state.value.busy)
        assertEquals(listOf("Where did my money go this month?" to AssistantMode.Cloud), interpreter.received)
    }

    @Test
    fun send_interpretFailures_mapToNotices() = runTest {
        val vm = vm()
        val expected = mapOf(
            InterpretFailure.ModelUnavailable to NoticeKind.ModelUnavailable,
            InterpretFailure.InvalidOutput to NoticeKind.InvalidResponse,
            InterpretFailure.CloudUnreachable to NoticeKind.CloudOffline,
        )
        expected.forEach { (failure, kind) ->
            interpreter.next = Interpretation.Failed(failure)
            vm.send("Where did my money go this month?")
            assertEquals(kind, vm.last<ChatItem.Notice>().kind)
        }
    }

    @Test
    fun send_unsupported_appendsUnsupportedNotice() = runTest {
        interpreter.next = Interpretation.Unsupported
        val vm = vm()
        vm.send("Should I buy shares?")
        val notice = vm.items.last()
        assertEquals(ChatItem.Notice(notice.id, AssistantMode.Cloud, NoticeKind.Unsupported, null), notice)
    }

    @Test
    fun send_onDevice_interpretsInOnDeviceModeAndCaptionsReply() = runTest {
        settings.setAssistantMode(AssistantMode.OnDevice)
        interpreter.next = Interpretation.Unsupported
        val vm = vm()
        vm.send("Should I buy shares?")
        assertEquals(listOf("Should I buy shares?" to AssistantMode.OnDevice), interpreter.received)
        assertEquals(AssistantMode.OnDevice, vm.last<ChatItem.Notice>().mode)
    }

    @Test
    fun send_spending_appendsSpendingResultWithFocusFood() = runTest {
        interpreter.next = Interpretation.Spending(november)
        val vm = vm()
        vm.send("Where did my money go this month?")
        val result = vm.items.last()
        val spending = SampleFinance.november.toUi()
        assertEquals(ChatItem.SpendingResult(result.id, AssistantMode.Cloud, spending, spending.categories[1]), result)
        assertEquals("food", (result as ChatItem.SpendingResult).focus?.id)
        assertEquals(2, vm.items.size)
    }

    @Test
    fun send_spendingEmpty_appendsNoData() = runTest {
        val march = YearMonth(2026, 3)
        interpreter.next = Interpretation.Spending(march)
        finance.spendingResult = AppResult.Success(SampleFinance.november.copy(month = march, categories = emptyList()))
        val vm = vm()
        vm.send("Where did my money go in March?")
        val notice = vm.items.last()
        assertEquals(ChatItem.Notice(notice.id, AssistantMode.Cloud, NoticeKind.NoData, march), notice)
    }

    @Test
    fun send_spendingFailure_appendsNoData() = runTest {
        interpreter.next = Interpretation.Spending(november)
        finance.spendingResult = AppResult.Failure(AppError.NotConnected)
        val vm = vm()
        vm.send("Where did my money go this month?")
        assertEquals(ChatItem.Notice(vm.items.last().id, AssistantMode.Cloud, NoticeKind.NoData, november), vm.items.last())
    }

    @Test
    fun send_categoryDetail_appendsOpeningTextAndOpensCategory() = runTest {
        interpreter.next = Interpretation.CategoryDetail("food", november)
        val vm = vm()
        vm.effects.test {
            vm.send("Show food and groceries")
            assertEquals(AssistantEffect.OpenCategory("food", november), awaitItem())
        }
        val text = vm.items.last()
        val expected = UiText.Res(R.string.assistant_opening_category, persistentListOf("Food and groceries", "November 2026"))
        assertEquals(ChatItem.AssistantText(text.id, AssistantMode.Cloud, expected), text)
    }

    @Test
    fun send_categoryDetailFailure_appendsNoData() = runTest {
        interpreter.next = Interpretation.CategoryDetail("food", november)
        finance.transactionsResult = AppResult.Failure(AppError.NotConnected)
        val vm = vm()
        vm.effects.test {
            vm.send("Show food and groceries")
            expectNoEvents()
        }
        assertEquals(ChatItem.Notice(vm.items.last().id, AssistantMode.Cloud, NoticeKind.NoData, november), vm.items.last())
    }

    @Test
    fun send_whileBusy_ignored() = runTest {
        val gated = GatedInterpreter(Interpretation.Unsupported)
        val vm = vm(interpreter = gated)
        vm.send("Where did my money go this month?")
        assertTrue(vm.state.value.busy)
        val thinking = vm.items.last()
        assertEquals(ChatItem.Thinking(thinking.id, ThinkingLabel.AskingCloud), thinking)

        vm.send("Help me create a budget")
        vm.onIntent(AssistantIntent.SuggestionTapped(Suggestion.Ask(Prompt.AffordLaptop)))
        assertEquals("Help me create a budget", vm.state.value.draft)
        assertEquals(listOf("Where did my money go this month?"), gated.received)

        gated.gate.complete(Unit)
        assertEquals(false, vm.state.value.busy)
        assertEquals(listOf(ChatItem.User::class, ChatItem.Notice::class), vm.items.map { it::class })
    }

    @Test
    fun send_onDevice_showsInterpretingOnDeviceWhileWaiting() = runTest {
        settings.setAssistantMode(AssistantMode.OnDevice)
        val gated = GatedInterpreter(Interpretation.Unsupported)
        val vm = vm(interpreter = gated)
        vm.send("Where did my money go this month?")
        assertEquals(ThinkingLabel.InterpretingOnDevice, vm.last<ChatItem.Thinking>().label)
        gated.gate.complete(Unit)
    }

    @Test
    fun suggestionAsk_appendsPromptLabelAndSendsQuery() = runTest {
        interpreter.next = Interpretation.Budget
        val vm = vm()
        vm.onIntent(AssistantIntent.DraftChanged("half-typed"))
        vm.onIntent(AssistantIntent.SuggestionTapped(Suggestion.Ask(Prompt.CreateBudget)))
        assertEquals(ChatItem.User(vm.items.first().id, UiText.Res(Prompt.CreateBudget.label)), vm.items.first())
        assertEquals(listOf("Help me create a budget" to AssistantMode.Cloud), interpreter.received)
        assertIs<ChatItem.Budget>(vm.items.last())
        assertEquals("", vm.state.value.draft)
    }

    // ---- Affordability ----

    @Test
    fun send_affordabilityWithoutPrice_asksPrice_thenNumberOpensDetails() = runTest {
        interpreter.next = Interpretation.Affordability(null)
        val vm = vm()
        vm.send("Can I afford a laptop?")
        val question = vm.items.last()
        assertEquals(ChatItem.PriceQuestion(question.id, AssistantMode.Cloud), question)

        interpreter.next = Interpretation.Unsupported // proves the price reply never reaches the interpreter
        vm.send("KSh 120,000")
        val details = vm.items.last()
        val form = AffordabilityFormUi(Money.kes(120_000), "", "", availableMissing = false, monthlyMissing = false, submitted = false)
        assertEquals(ChatItem.AffordabilityDetails(details.id, AssistantMode.Cloud, form), details)
        assertEquals(listOf("Can I afford a laptop?" to AssistantMode.Cloud), interpreter.received)
        assertEquals(ChatItem.User(vm.items[vm.items.size - 2].id, UiText.Raw("KSh 120,000")), vm.items[vm.items.size - 2])
    }

    @Test
    fun send_awaitingPrice_budgetOrSpendingTextGoesToInterpreter() = runTest {
        interpreter.next = Interpretation.Affordability(null)
        val vm = vm()
        vm.send("Can I afford a laptop?")
        interpreter.next = Interpretation.Budget
        vm.send("Make a budget of 50,000")
        assertIs<ChatItem.Budget>(vm.items.last())
        interpreter.next = Interpretation.Spending(november)
        vm.send("What did I spend over 1,000 on?")
        assertIs<ChatItem.SpendingResult>(vm.items.last())
        assertEquals(3, interpreter.received.size)
    }

    @Test
    fun send_affordabilityWithPrice_opensDetailsDirectly() = runTest {
        val vm = vm()
        val details = vm.openDetails()
        assertEquals(Money.kes(90_000), details.form.price)
        assertEquals(1, vm.items.filterIsInstance<ChatItem.AffordabilityDetails>().size)
    }

    @Test
    fun submitForm_missing_setsFlags() = runTest {
        val vm = vm()
        val details = vm.openDetails()
        vm.onIntent(AssistantIntent.SubmitForm(details.id))
        assertEquals(true to true, vm.last<ChatItem.AffordabilityDetails>().form.let { it.availableMissing to it.monthlyMissing })

        vm.onIntent(AssistantIntent.FormAvailable(details.id, "30,000"))
        vm.onIntent(AssistantIntent.SubmitForm(details.id))
        val form = vm.last<ChatItem.AffordabilityDetails>().form
        assertEquals("30000", form.available)
        assertEquals(false, form.availableMissing)
        assertEquals(true, form.monthlyMissing)
        assertEquals(false, form.submitted)
        assertEquals(details.id, vm.items.last().id)
    }

    @Test
    fun formFields_keepDigitsOnly_andClearMissingFlagWhenFilled() = runTest {
        val vm = vm()
        val details = vm.openDetails()
        vm.onIntent(AssistantIntent.SubmitForm(details.id))
        vm.onIntent(AssistantIntent.FormMonthly(details.id, "KSh 1x0,0o0"))
        val form = vm.last<ChatItem.AffordabilityDetails>().form
        assertEquals("1000", form.monthly)
        assertEquals(false, form.monthlyMissing)
        assertEquals(true, form.availableMissing)
    }

    @Test
    fun submitForm_valid_appendsUserSummaryAndOnTrack6Months() = runTest {
        val vm = vm()
        val details = vm.openDetails()
        vm.onIntent(AssistantIntent.FormAvailable(details.id, "30000"))
        vm.onIntent(AssistantIntent.FormMonthly(details.id, "10000"))
        vm.onIntent(AssistantIntent.SubmitForm(details.id))

        assertEquals(true, vm.items.filterIsInstance<ChatItem.AffordabilityDetails>().single().form.submitted)
        val (user, result) = vm.items.takeLast(2)
        val summary = UiText.Res(R.string.assistant_afford_details_summary, persistentListOf("KSh 30,000", "KSh 10,000"))
        assertEquals(ChatItem.User(user.id, summary), user)
        val expected = AffordabilityUi(
            price = Money.kes(90_000),
            available = Money.kes(30_000),
            monthly = Money.kes(10_000),
            outcome = AffordabilityOutcome.OnTrack(6, Money.kes(60_000), YearMonth(2027, 5)),
            percentSetAside = 33,
            monthBlocks = monthBlocks(november, 6),
        )
        assertEquals(ChatItem.AffordabilityResult(result.id, AssistantMode.Cloud, expected), result)
    }

    @Test
    fun submitForm_twice_appendsResultOnce() = runTest {
        val vm = vm()
        val details = vm.openDetails()
        vm.onIntent(AssistantIntent.UseSampleValues(details.id))
        vm.onIntent(AssistantIntent.SubmitForm(details.id))
        vm.onIntent(AssistantIntent.SubmitForm(details.id))
        vm.onIntent(AssistantIntent.FormAvailable(details.id, "1"))
        assertEquals(1, vm.items.filterIsInstance<ChatItem.AffordabilityResult>().size)
        assertEquals("30000", vm.last<ChatItem.AffordabilityDetails>().form.available)
    }

    @Test
    fun useSampleValues_fillsFormWithPrototypeValues() = runTest {
        val vm = vm()
        val details = vm.openDetails()
        vm.onIntent(AssistantIntent.SubmitForm(details.id))
        vm.onIntent(AssistantIntent.UseSampleValues(details.id))
        val form = vm.last<ChatItem.AffordabilityDetails>().form
        assertEquals(AffordabilityFormUi(Money.kes(90_000), "30000", "10000", false, false, false), form)
    }

    @Test
    fun stepMonthly_down_stopsAtZero_andShowsNoEndDate() = runTest {
        val vm = vm()
        val result = vm.openResult()
        repeat(3) { vm.onIntent(AssistantIntent.StepMonthly(result.id, StepDirection.Down)) }
        val ui = vm.result(result.id).ui
        assertEquals(Money.Zero, ui.monthly)
        assertEquals(AffordabilityOutcome.NoEndDate(Money.kes(60_000)), ui.outcome)
        assertEquals(persistentListOf(), ui.monthBlocks)
    }

    @Test
    fun stepAvailable_upAndDown_recomputesAndStopsAtZero() = runTest {
        val vm = vm()
        val result = vm.openResult()
        vm.onIntent(AssistantIntent.StepAvailable(result.id, StepDirection.Up))
        assertEquals(AffordabilityOutcome.OnTrack(5, Money.kes(50_000), YearMonth(2027, 4)), vm.result(result.id).ui.outcome)
        assertEquals(44, vm.result(result.id).ui.percentSetAside)
        repeat(6) { vm.onIntent(AssistantIntent.StepAvailable(result.id, StepDirection.Down)) }
        assertEquals(Money.Zero, vm.result(result.id).ui.available)
        assertEquals(AffordabilityOutcome.OnTrack(9, Money.kes(90_000), YearMonth(2027, 8)), vm.result(result.id).ui.outcome)
    }

    @Test
    fun trySaving15k_setsMonthlyTo15000() = runTest {
        val vm = vm()
        val result = vm.openResult()
        vm.onIntent(AssistantIntent.SuggestionTapped(Suggestion.TrySaving15k))
        val ui = vm.result(result.id).ui
        assertEquals(Money.kes(15_000), ui.monthly)
        assertEquals(AffordabilityOutcome.OnTrack(4, Money.kes(60_000), YearMonth(2027, 3)), ui.outcome)
        assertEquals(monthBlocks(november, 4), ui.monthBlocks)
    }

    // ---- Budget ----

    @Test
    fun send_budget_appendsDraft() = runTest {
        val vm = vm()
        val budget = vm.openBudget()
        assertEquals(ChatItem.Budget(budget.id, AssistantMode.Cloud, SampleFinance.draft.toUi(BudgetStatus.Draft)), budget)
    }

    @Test
    fun send_budgetFailure_appendsNoDataForCurrentMonth() = runTest {
        finance.draftResult = AppResult.Failure(AppError.NotConnected)
        val vm = vm()
        interpreter.next = Interpretation.Budget
        vm.send("Help me create a budget")
        assertEquals(ChatItem.Notice(vm.items.last().id, AssistantMode.Cloud, NoticeKind.NoData, november), vm.items.last())
    }

    @Test
    fun incomeChanged_nonDigits_becomesZero() = runTest {
        val vm = vm()
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.IncomeChanged(budget.id, "abc"))
        val ui = vm.budget(budget.id).ui
        assertEquals(Money.Zero, ui.income)
        assertEquals(Money.kes(-75_000), ui.totals.difference)
        assertEquals(true, ui.totals.isOver)
    }

    @Test
    fun lineChanged_hugeValue_clampsTo10M() = runTest {
        val vm = vm()
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.LineChanged(budget.id, 0, "99999999999999999999999"))
        assertEquals(Money.kes(10_000_000), vm.budget(budget.id).ui.lines[0].limit)
        vm.onIntent(AssistantIntent.LineChanged(budget.id, 1, "12,500"))
        val ui = vm.budget(budget.id).ui
        assertEquals(Money.kes(12_500), ui.lines[1].limit)
        assertEquals(Money.kes(10_000_000 + 12_500 + 10_000 + 7_000 + 8_000 + 10_000), ui.totals.total)
    }

    @Test
    fun lineStep_movesBy500_andStaysWithinRange() = runTest {
        val vm = vm()
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.LineStep(budget.id, 2, StepDirection.Up))
        assertEquals(Money.kes(10_500), vm.budget(budget.id).ui.lines[2].limit)
        assertEquals(Money.kes(75_500), vm.budget(budget.id).ui.totals.total)
        vm.onIntent(AssistantIntent.LineChanged(budget.id, 3, "200"))
        vm.onIntent(AssistantIntent.LineStep(budget.id, 3, StepDirection.Down))
        assertEquals(Money.Zero, vm.budget(budget.id).ui.lines[3].limit)
        vm.onIntent(AssistantIntent.LineChanged(budget.id, 4, "10000000"))
        vm.onIntent(AssistantIntent.LineStep(budget.id, 4, StepDirection.Up))
        assertEquals(Money.kes(10_000_000), vm.budget(budget.id).ui.lines[4].limit)
        vm.onIntent(AssistantIntent.LineStep(budget.id, 99, StepDirection.Up))
        assertEquals(6, vm.budget(budget.id).ui.lines.size)
    }

    @Test
    fun cancelBudget_discardsAndOffersCreateBudget() = runTest {
        val vm = vm()
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.CancelBudget(budget.id))
        assertEquals(BudgetStatus.Discarded, vm.budget(budget.id).ui.status)
        val text = vm.items.last()
        val expected = ChatItem.AssistantText(
            text.id,
            AssistantMode.Cloud,
            UiText.Res(R.string.assistant_budget_discarded),
            afterCancel = true,
        )
        assertEquals(expected, text)
        assertEquals(persistentListOf(Suggestion.Ask(Prompt.CreateBudget)), suggestionsAfter(text))
    }

    @Test
    fun budgetEdits_ignoredUnlessDraft() = runTest {
        val vm = vm()
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.CancelBudget(budget.id))
        val discarded = vm.budget(budget.id)
        vm.onIntent(AssistantIntent.IncomeChanged(budget.id, "5"))
        vm.onIntent(AssistantIntent.LineChanged(budget.id, 0, "5"))
        vm.onIntent(AssistantIntent.LineStep(budget.id, 0, StepDirection.Up))
        vm.onIntent(AssistantIntent.CancelBudget(budget.id))
        vm.onIntent(AssistantIntent.ReviewBudget(budget.id))
        assertEquals(discarded, vm.budget(budget.id))
        assertEquals(1, vm.items.filterIsInstance<ChatItem.AssistantText>().size)
        assertEquals(AssistantDialog.None, vm.state.value.dialog)
    }

    @Test
    fun reviewBudget_opensDialog_andDismissCloses() = runTest {
        val vm = vm()
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.ReviewBudget(budget.id))
        assertEquals(AssistantDialog.ReviewBudget(budget.id), vm.state.value.dialog)
        vm.onIntent(AssistantIntent.DismissDialog)
        assertEquals(AssistantDialog.None, vm.state.value.dialog)
        assertEquals(emptyList(), finance.savedDrafts)
    }

    @Test
    fun confirmSave_success_savesOnceAndAppendsSummary() = runTest {
        val vm = vm()
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.ReviewBudget(budget.id))
        vm.onIntent(AssistantIntent.ConfirmSave)

        assertEquals(listOf(SampleFinance.draft), finance.savedDrafts)
        assertEquals(AssistantDialog.None, vm.state.value.dialog)
        assertEquals(BudgetStatus.Saved, vm.budget(budget.id).ui.status)
        val saved = vm.items.last()
        val summary = UiText.Plural(R.plurals.assistant_budget_saved_unallocated, 6, persistentListOf("KSh 75,000", 6, "KSh 25,000"))
        assertEquals(ChatItem.BudgetSaved(saved.id, AssistantMode.Cloud, november, summary), saved)
        assertEquals(listOf(ChatItem.Budget::class, ChatItem.BudgetSaved::class), vm.items.takeLast(2).map { it::class })
        assertEquals(false, vm.state.value.busy)
    }

    @Test
    fun confirmSave_overIncome_summarisesOverspend() = runTest {
        val vm = vm()
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.IncomeChanged(budget.id, "70000"))
        vm.onIntent(AssistantIntent.ReviewBudget(budget.id))
        vm.onIntent(AssistantIntent.ConfirmSave)
        val summary = UiText.Plural(R.plurals.assistant_budget_saved_over_income, 6, persistentListOf("KSh 75,000", 6, "KSh 5,000"))
        assertEquals(summary, vm.last<ChatItem.BudgetSaved>().summary)
        assertEquals(Money.kes(70_000), finance.savedDrafts.single().income)
    }

    @Test
    fun confirmSave_twice_savesOnce() = runTest {
        val gated = GatedFinance(finance)
        val vm = vm(finance = gated)
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.ReviewBudget(budget.id))
        vm.onIntent(AssistantIntent.ConfirmSave)
        assertEquals(ThinkingLabel.Saving, vm.last<ChatItem.Thinking>().label)
        assertTrue(vm.state.value.busy)

        vm.onIntent(AssistantIntent.ConfirmSave)
        vm.onIntent(AssistantIntent.ReviewBudget(budget.id))
        vm.onIntent(AssistantIntent.ConfirmSave)
        gated.gate.complete(Unit)
        vm.onIntent(AssistantIntent.ReviewBudget(budget.id))
        vm.onIntent(AssistantIntent.ConfirmSave)

        assertEquals(1, gated.saveCalls)
        assertEquals(1, vm.items.filterIsInstance<ChatItem.BudgetSaved>().size)
        assertEquals(BudgetStatus.Saved, vm.budget(budget.id).ui.status)
    }

    @Test
    fun cancelBudget_duringSave_isIgnored() = runTest {
        val gated = GatedFinance(finance)
        val vm = vm(finance = gated)
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.ReviewBudget(budget.id))
        vm.onIntent(AssistantIntent.ConfirmSave)

        vm.onIntent(AssistantIntent.CancelBudget(budget.id))
        vm.onIntent(AssistantIntent.ReviewBudget(budget.id))
        assertEquals(BudgetStatus.Draft, vm.budget(budget.id).ui.status)
        assertEquals(AssistantDialog.None, vm.state.value.dialog)

        gated.gate.complete(Unit)
        assertEquals(BudgetStatus.Saved, vm.budget(budget.id).ui.status)
        assertEquals(listOf(ChatItem.User::class, ChatItem.Budget::class, ChatItem.BudgetSaved::class), vm.items.map { it::class })
    }

    @Test
    fun lineChanged_duringSave_isIgnored() = runTest {
        val gated = GatedFinance(finance)
        val vm = vm(finance = gated)
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.ReviewBudget(budget.id))
        vm.onIntent(AssistantIntent.ConfirmSave)

        vm.onIntent(AssistantIntent.LineChanged(budget.id, 0, "1"))
        vm.onIntent(AssistantIntent.LineStep(budget.id, 1, StepDirection.Up))
        vm.onIntent(AssistantIntent.IncomeChanged(budget.id, "1"))
        assertEquals(SampleFinance.draft.toUi(BudgetStatus.Draft), vm.budget(budget.id).ui)

        gated.gate.complete(Unit)
        assertEquals(SampleFinance.draft.toUi(BudgetStatus.Saved), vm.budget(budget.id).ui)
        assertEquals(listOf(SampleFinance.draft), finance.savedDrafts)
        assertEquals(listOf(ChatItem.User::class, ChatItem.Budget::class, ChatItem.BudgetSaved::class), vm.items.map { it::class })
    }

    @Test
    fun send_interpreterThrows_stillClearsThinkingAndBusy() {
        val throwing = object : IntentInterpreter {
            override suspend fun interpret(text: String, mode: AssistantMode): Interpretation = error("boom")
        }
        val vm = vm(interpreter = throwing)
        // The exception still escapes to the ViewModel scope, which runTest reports once the body has run.
        val thrown = assertFailsWith<IllegalStateException> {
            runTest {
                vm.send("Where did my money go this month?")
                assertEquals(false, vm.state.value.busy)
                assertEquals(listOf(ChatItem.User::class), vm.items.map { it::class })
            }
        }
        assertEquals("boom", thrown.message)
    }

    @Test
    fun confirmSave_withoutDialog_doesNothing() = runTest {
        val vm = vm()
        vm.openBudget()
        vm.onIntent(AssistantIntent.ConfirmSave)
        assertEquals(emptyList(), finance.savedDrafts)
    }

    @Test
    fun confirmSave_failure_appendsSaveFailedWithRetry() = runTest {
        finance.saveResult = AppResult.Failure(AppError.Storage)
        val vm = vm()
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.ReviewBudget(budget.id))
        vm.onIntent(AssistantIntent.ConfirmSave)

        val notice = vm.items.last()
        assertEquals(ChatItem.Notice(notice.id, AssistantMode.Cloud, NoticeKind.SaveFailed, null), notice)
        assertEquals(BudgetStatus.Draft, vm.budget(budget.id).ui.status)
        assertEquals(AssistantDialog.None, vm.state.value.dialog)

        finance.saveResult = AppResult.Success(Unit)
        vm.onIntent(AssistantIntent.NoticeActionTapped(NoticeAction.RetrySave))
        assertEquals(AssistantDialog.ReviewBudget(budget.id), vm.state.value.dialog)
        vm.onIntent(AssistantIntent.ConfirmSave)
        assertEquals(BudgetStatus.Saved, vm.budget(budget.id).ui.status)
        assertEquals(2, finance.savedDrafts.size)
    }

    // ---- Load, clears and notice actions ----

    @Test
    fun load_followsModeAndModelStatus() = runTest {
        val vm = vm()
        assertEquals(AssistantMode.Cloud, vm.state.value.mode)
        assertEquals(false, vm.state.value.modelReady)
        settings.setAssistantMode(AssistantMode.OnDevice)
        models.statusFlow.value = ModelStatus.Ready
        assertEquals(AssistantMode.OnDevice, vm.state.value.mode)
        assertEquals(true, vm.state.value.modelReady)
        models.statusFlow.value = ModelStatus.Downloading(40)
        assertEquals(false, vm.state.value.modelReady)
    }

    @Test
    fun load_twice_collectsOnce() = runTest {
        val counting = CountingSettings(settings)
        val vm = vm(settings = counting)
        vm.onIntent(AssistantIntent.Load)
        assertEquals(1, counting.subscriptions)
    }

    @Test
    fun conversationCleared_emptiesItems() = runTest {
        interpreter.next = Interpretation.Affordability(null)
        val vm = vm()
        vm.send("Can I afford a laptop?")
        val budget = vm.openBudget()
        vm.onIntent(AssistantIntent.ReviewBudget(budget.id))
        val lastId = vm.items.last().id

        conversation.clear()
        assertEquals(persistentListOf(), vm.items)
        assertEquals(AssistantDialog.None, vm.state.value.dialog)

        // No longer awaiting a price, so the number goes to the interpreter.
        interpreter.next = Interpretation.Unsupported
        vm.send("KSh 120,000")
        assertEquals("KSh 120,000", interpreter.received.last().first)
        assertIs<ChatItem.Notice>(vm.items.last())
        assertTrue(vm.items.all { it.id > lastId }, "item ids are never reused")
    }

    @Test
    fun conversationCleared_whileBusy_dropsTheReply() = runTest {
        val gated = GatedInterpreter(Interpretation.Unsupported)
        val vm = vm(interpreter = gated)
        vm.send("Where did my money go this month?")
        conversation.clear()
        assertEquals(false, vm.state.value.busy)
        gated.gate.complete(Unit)
        assertEquals(persistentListOf(), vm.items)
    }

    @Test
    fun askAgain_resendsLastQuery() = runTest {
        interpreter.next = Interpretation.Failed(InterpretFailure.CloudUnreachable)
        val vm = vm()
        vm.onIntent(AssistantIntent.SuggestionTapped(Suggestion.Ask(Prompt.SpendingThisMonth)))
        interpreter.next = Interpretation.Spending(november)
        vm.onIntent(AssistantIntent.NoticeActionTapped(NoticeAction.AskAgain))

        val query = "Where did my money go this month?"
        assertEquals(listOf(query to AssistantMode.Cloud, query to AssistantMode.Cloud), interpreter.received)
        val users = vm.items.filterIsInstance<ChatItem.User>()
        assertEquals(listOf(UiText.Res(Prompt.SpendingThisMonth.label), UiText.Res(Prompt.SpendingThisMonth.label)), users.map { it.text })
        assertIs<ChatItem.SpendingResult>(vm.items.last())
    }

    @Test
    fun askAgain_whileBusy_ignored() = runTest {
        val gated = GatedInterpreter(Interpretation.Unsupported)
        val vm = vm(interpreter = gated)
        vm.send("Where did my money go this month?")
        vm.onIntent(AssistantIntent.NoticeActionTapped(NoticeAction.AskAgain))
        assertEquals(1, gated.received.size)
        assertEquals(1, vm.items.filterIsInstance<ChatItem.User>().size)
        gated.gate.complete(Unit)
    }

    @Test
    fun showThisMonth_asksSpendingThisMonth() = runTest {
        interpreter.next = Interpretation.Spending(november)
        val vm = vm()
        vm.onIntent(AssistantIntent.NoticeActionTapped(NoticeAction.ShowThisMonth))
        assertEquals(listOf("Where did my money go this month?" to AssistantMode.Cloud), interpreter.received)
        assertEquals(UiText.Res(Prompt.SpendingThisMonth.label), vm.items.filterIsInstance<ChatItem.User>().single().text)
    }

    @Test
    fun useCloud_withoutConsent_opensConsent() = runTest {
        val settings = FakeSettingsRepository(AppSettings(assistantMode = AssistantMode.OnDevice, cloudConsentGiven = false))
        val vm = vm(settings = settings)
        vm.effects.test {
            vm.onIntent(AssistantIntent.NoticeActionTapped(NoticeAction.UseCloud))
            assertEquals(AssistantEffect.OpenCloudConsent, awaitItem())
        }
        assertEquals(AssistantMode.OnDevice, settings.settings.first().assistantMode)
        assertEquals(emptyList(), messages.shown)
    }

    @Test
    fun useCloud_withConsent_switchesAndShowsMessage() = runTest {
        settings.setAssistantMode(AssistantMode.OnDevice)
        val vm = vm()
        vm.effects.test {
            vm.onIntent(AssistantIntent.NoticeActionTapped(NoticeAction.UseCloud))
            expectNoEvents()
        }
        assertEquals(AssistantMode.Cloud, settings.settings.first().assistantMode)
        assertEquals(AssistantMode.Cloud, vm.state.value.mode)
        assertEquals(listOf<UiText>(UiText.Res(R.string.assistant_message_switched_cloud)), messages.shown)
    }

    @Test
    fun useOnDevice_switchesAndShowsMessage() = runTest {
        val vm = vm()
        vm.onIntent(AssistantIntent.NoticeActionTapped(NoticeAction.UseOnDevice))
        assertEquals(AssistantMode.OnDevice, settings.settings.first().assistantMode)
        assertEquals(listOf<UiText>(UiText.Res(R.string.assistant_message_switched_on_device)), messages.shown)
    }

    @Test
    fun setUpModel_opensModelSetupFromAssistant() = runTest {
        val vm = vm()
        vm.effects.test {
            vm.onIntent(AssistantIntent.NoticeActionTapped(NoticeAction.SetUpModel))
            assertEquals(AssistantEffect.OpenModelSetup(ModelSetupOrigin.Assistant), awaitItem())
        }
    }

    @Test
    fun openCategory_fromSpendingResult_usesResultMonth() = runTest {
        val september = YearMonth(2026, 9)
        interpreter.next = Interpretation.Spending(september)
        finance.spendingResult = AppResult.Success(SampleFinance.november.copy(month = september))
        val vm = vm()
        vm.send("Where did my money go in September?")
        val result = vm.last<ChatItem.SpendingResult>()
        val chip = suggestionsAfter(result).first()
        assertEquals(Suggestion.OpenCategory("food", "Food and groceries", september), chip)
        vm.effects.test {
            vm.onIntent(AssistantIntent.SuggestionTapped(chip))
            assertEquals(AssistantEffect.OpenCategory("food", september), awaitItem())
            vm.onIntent(AssistantIntent.OpenCategory("rent", result.spending.month))
            assertEquals(AssistantEffect.OpenCategory("rent", september), awaitItem())
        }
    }

    // ---- Current month ----

    @Test
    fun noData_state_carriesCurrentMonth() = runTest {
        val march = YearMonth(2026, 3)
        interpreter.next = Interpretation.Spending(march)
        finance.spendingResult = AppResult.Success(SampleFinance.november.copy(month = march, categories = emptyList()))
        val vm = vm()
        vm.send("Where did my money go in March?")
        assertEquals(march, vm.last<ChatItem.Notice>().month)
        assertEquals(november, vm.state.value.currentMonth)
    }

    @Test
    fun currentMonth_setBeforeLoad_andRefreshedByLoad() = runTest {
        val vm = AssistantViewModel(interpreter, finance, settings, models, conversation, messages, clock, TimeZone.UTC)
        assertEquals(november, vm.state.value.currentMonth)
        clock.advanceBy(25.days) // 2026-12-05
        vm.onIntent(AssistantIntent.Load)
        assertEquals(YearMonth(2026, 12), vm.state.value.currentMonth)
    }

    @Test
    fun currentMonth_usesInjectedTimeZone() = runTest {
        clock.now = Instant.parse("2026-11-30T22:00:00Z") // already 1 December in Nairobi (UTC+3)
        val nairobi = AssistantViewModel(interpreter, finance, settings, models, conversation, messages, clock, TimeZone.of("Africa/Nairobi"))
        assertEquals(YearMonth(2026, 12), nairobi.state.value.currentMonth)
        val utc = AssistantViewModel(interpreter, finance, settings, models, conversation, messages, clock, TimeZone.UTC)
        assertEquals(november, utc.state.value.currentMonth)
    }

    @Test
    fun openSettings_emitsEffect() = runTest {
        val vm = vm()
        vm.effects.test {
            vm.onIntent(AssistantIntent.OpenSettings)
            assertEquals(AssistantEffect.OpenSettings, awaitItem())
        }
    }

    @Test
    fun itemIds_areUnique() = runTest {
        val vm = vm()
        vm.openResult()
        vm.openBudget()
        val ids = vm.items.map { it.id }
        assertEquals(ids.distinct(), ids)
        assertNull(vm.items.firstOrNull { it is ChatItem.Thinking })
    }

    /** Suspends every interpretation until [gate] completes. */
    private class GatedInterpreter(private val result: Interpretation) : IntentInterpreter {
        val gate = CompletableDeferred<Unit>()
        val received = mutableListOf<String>()

        override suspend fun interpret(text: String, mode: AssistantMode): Interpretation {
            received += text
            gate.await()
            return result
        }
    }

    /** Suspends every save until [gate] completes; [saveCalls] counts calls as they arrive. */
    private class GatedFinance(private val delegate: FakeFinanceRepository) : FinanceRepository by delegate {
        val gate = CompletableDeferred<Unit>()
        var saveCalls = 0
            private set

        override suspend fun saveBudget(draft: BudgetDraft): AppResult<Unit> {
            saveCalls++
            gate.await()
            return delegate.saveBudget(draft)
        }
    }

    private class CountingSettings(private val delegate: FakeSettingsRepository) : SettingsRepository by delegate {
        var subscriptions = 0
            private set

        override val settings: Flow<AppSettings> = delegate.settings.onStart { subscriptions++ }
    }
}
