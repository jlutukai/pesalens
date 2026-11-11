package lutukai.pesalens.feature.assistant.impl.presentation

import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import lutukai.pesalens.core.common.AppResult
import lutukai.pesalens.core.domain.ai.IntentInterpreter
import lutukai.pesalens.core.domain.ai.InterpretFailure
import lutukai.pesalens.core.domain.ai.Interpretation
import lutukai.pesalens.core.domain.ai.ModelManager
import lutukai.pesalens.core.domain.calc.AffordabilityCalculator
import lutukai.pesalens.core.domain.calc.BudgetCalculator
import lutukai.pesalens.core.domain.calc.PriceParser
import lutukai.pesalens.core.domain.repository.ConversationSession
import lutukai.pesalens.core.domain.repository.FinanceRepository
import lutukai.pesalens.core.domain.repository.SettingsRepository
import lutukai.pesalens.core.model.AffordabilityOutcome
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.ui.catalog.model.AffordabilityFormUi
import lutukai.pesalens.core.ui.catalog.model.AffordabilityUi
import lutukai.pesalens.core.ui.catalog.model.BudgetStatus
import lutukai.pesalens.core.ui.catalog.model.BudgetUi
import lutukai.pesalens.core.ui.catalog.model.NoticeKind
import lutukai.pesalens.core.ui.catalog.model.StepDirection
import lutukai.pesalens.core.ui.catalog.model.monthBlocks
import lutukai.pesalens.core.ui.format.formatKes
import lutukai.pesalens.core.ui.format.formatLong
import lutukai.pesalens.core.ui.message.UserMessageSink
import lutukai.pesalens.core.ui.mvi.MviViewModel
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.assistant.impl.R
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin

/** Upper bound for every typed or stepped amount, in whole shillings (prototype setLine). */
private const val MaxShillings = 10_000_000L
private const val MonthlyStep = 5_000L
private const val AvailableStep = 10_000L
private const val BudgetLineStep = 500L
private const val TrySavingMonthly = 15_000L
private const val SampleAvailable = "30000"
private const val SampleMonthly = "10000"

/** A reply that mentions a budget or spending is a new question, even while a price is awaited (prototype route). */
private val BudgetOrSpending = Regex("budget|spen", RegexOption.IGNORE_CASE)

/**
 * The conversation state machine, ported from the prototype's ask/respond/route. Questions go to the
 * [IntentInterpreter]; every figure is computed here from repository data. One question or save is in flight at a
 * time, and nothing is ever written without the review dialog's confirmation.
 */
class AssistantViewModel(
    private val interpreter: IntentInterpreter,
    private val finance: FinanceRepository,
    private val settings: SettingsRepository,
    private val modelManager: ModelManager,
    private val conversation: ConversationSession,
    private val messages: UserMessageSink,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : MviViewModel<AssistantUiState, AssistantIntent, AssistantEffect>(
    AssistantUiState(currentMonth = clock.currentMonth(timeZone)),
) {

    /** A sent question: what the user bubble shows and what the interpreter receives. */
    private data class Question(val text: UiText, val query: String)

    private var loadJob: Job? = null
    private var requestJob: Job? = null

    /** Bumped per request and on clear, so a cancelled request's cleanup never touches a newer one's state. */
    private var requestGeneration = 0
    private var modeJob: Job? = null
    private var lastItemId = 0L
    private var awaitingPrice = false
    private var lastQuestion: Question? = null

    override fun handleIntent(intent: AssistantIntent) = when (intent) {
        AssistantIntent.Load -> load()
        is AssistantIntent.DraftChanged -> reduce { copy(draft = intent.text) }
        AssistantIntent.Send -> send()
        is AssistantIntent.SuggestionTapped -> suggestion(intent.s)
        is AssistantIntent.OpenCategory -> emit(AssistantEffect.OpenCategory(intent.categoryId, intent.month))
        is AssistantIntent.FormAvailable -> editForm(intent.id) {
            val available = intent.digits.toAmountDigits()
            copy(available = available, availableMissing = availableMissing && available.isEmpty())
        }
        is AssistantIntent.FormMonthly -> editForm(intent.id) {
            val monthly = intent.digits.toAmountDigits()
            copy(monthly = monthly, monthlyMissing = monthlyMissing && monthly.isEmpty())
        }
        is AssistantIntent.UseSampleValues -> editForm(intent.id) {
            copy(available = SampleAvailable, monthly = SampleMonthly, availableMissing = false, monthlyMissing = false)
        }
        is AssistantIntent.SubmitForm -> submitForm(intent.id)
        is AssistantIntent.StepMonthly -> editResult(intent.id) {
            recalculate(monthly = monthly.step(intent.dir, MonthlyStep))
        }
        is AssistantIntent.StepAvailable -> editResult(intent.id) {
            recalculate(available = available.step(intent.dir, AvailableStep))
        }
        is AssistantIntent.IncomeChanged -> editBudget(intent.id) { copy(income = Money.kes(intent.digits.toShillings())) }
        is AssistantIntent.LineChanged -> editBudget(intent.id) {
            withLine(intent.index) { Money.kes(intent.digits.toShillings()) }
        }
        is AssistantIntent.LineStep -> editBudget(intent.id) {
            withLine(intent.index) { it.step(intent.dir, BudgetLineStep).coerceAtMost(Money.kes(MaxShillings)) }
        }
        is AssistantIntent.CancelBudget -> cancelBudget(intent.id)
        is AssistantIntent.ReviewBudget -> reviewBudget(intent.id)
        AssistantIntent.DismissDialog -> reduce { copy(dialog = AssistantDialog.None) }
        AssistantIntent.ConfirmSave -> confirmSave()
        is AssistantIntent.NoticeActionTapped -> noticeAction(intent.action)
        AssistantIntent.OpenSettings -> emit(AssistantEffect.OpenSettings)
    }

    /**
     * Refreshes [AssistantUiState.currentMonth] on every entry, then collects mode, model readiness and conversation
     * clears once, however often the screen re-enters.
     */
    private fun load() {
        reduce { copy(currentMonth = currentMonth()) }
        if (loadJob != null) return
        loadJob = viewModelScope.launch {
            launch {
                settings.settings.map { it.assistantMode }.distinctUntilChanged().collect { reduce { copy(mode = it) } }
            }
            launch {
                modelManager.status.map { it is ModelStatus.Ready }.distinctUntilChanged()
                    .collect { reduce { copy(modelReady = it) } }
            }
            launch { conversation.clears.collect { clearConversation() } }
        }
    }

    /** Drops every item and any reply still in flight. Item ids keep counting up, so none is ever reused. */
    private fun clearConversation() {
        requestGeneration++
        requestJob?.cancel()
        requestJob = null
        awaitingPrice = false
        lastQuestion = null
        reduce { copy(items = persistentListOf(), busy = false, dialog = AssistantDialog.None) }
    }

    // ---- Questions ----

    private fun send() {
        val text = currentState.draft.trim()
        if (text.isEmpty()) return
        ask(Question(UiText.Raw(text), text))
    }

    private fun ask(prompt: Prompt) = ask(Question(UiText.Res(prompt.label), prompt.query))

    /**
     * Prototype ask: the user bubble (clearing the draft, as prototype userMsg does), then the reply routed from the
     * question. Ignored while busy.
     */
    private fun ask(question: Question) {
        if (currentState.busy) return
        lastQuestion = question
        val user = ChatItem.User(nextId(), question.text)
        reduce { copy(items = (items + user).toImmutableList(), draft = "") }
        respond(label = null) { mode -> route(question.query, mode) }
    }

    /**
     * Prototype respond: marks [AssistantUiState.busy], shows a Thinking item ([label], or who interprets in the
     * current mode when null), then swaps it for the replies [build] returns. The mode is read from the repository
     * so a request made before the first settings emission still uses the stored mode. Thinking is removed and busy
     * cleared even if [build] throws; a request dropped by a conversation clear leaves the state to the clear.
     */
    private fun respond(label: ThinkingLabel?, build: suspend (AssistantMode) -> List<ChatItem>) {
        reduce { copy(busy = true) }
        val generation = ++requestGeneration
        requestJob = viewModelScope.launch {
            var thinkingId: Long? = null
            var replies = emptyList<ChatItem>()
            try {
                val mode = settings.settings.first().assistantMode
                val thinking = ChatItem.Thinking(nextId(), label ?: mode.thinkingLabel())
                thinkingId = thinking.id
                append(thinking)
                replies = build(mode)
            } finally {
                if (generation == requestGeneration) {
                    reduce { copy(items = (items.filterNot { it.id == thinkingId } + replies).toImmutableList(), busy = false) }
                }
            }
        }
    }

    private suspend fun route(query: String, mode: AssistantMode): List<ChatItem> {
        if (awaitingPrice && !BudgetOrSpending.containsMatchIn(query)) {
            PriceParser.parse(query)?.let { return listOf(details(mode, it)) }
        }
        return when (val interpretation = interpreter.interpret(query, mode)) {
            is Interpretation.Spending -> spending(interpretation.month, mode)
            is Interpretation.CategoryDetail -> categoryDetail(interpretation.categoryId, interpretation.month, mode)
            is Interpretation.Affordability -> listOf(interpretation.price?.let { details(mode, it) } ?: priceQuestion(mode))
            Interpretation.Budget -> budget(mode)
            Interpretation.Unsupported -> listOf(notice(mode, NoticeKind.Unsupported))
            is Interpretation.Failed -> listOf(notice(mode, interpretation.reason.toNoticeKind()))
        }
    }

    private suspend fun spending(month: YearMonth, mode: AssistantMode): List<ChatItem> =
        when (val result = finance.spending(month)) {
            is AppResult.Success -> if (result.value.categories.isEmpty()) {
                listOf(notice(mode, NoticeKind.NoData, month))
            } else {
                val ui = result.value.toUi()
                val focus = ui.categories.firstOrNull { it.id == result.value.focusCategoryId }
                listOf(ChatItem.SpendingResult(nextId(), mode, ui, focus))
            }
            is AppResult.Failure -> listOf(notice(mode, NoticeKind.NoData, month))
        }

    private suspend fun categoryDetail(categoryId: String, month: YearMonth, mode: AssistantMode): List<ChatItem> =
        when (val result = finance.categoryTransactions(categoryId, month)) {
            is AppResult.Success -> {
                emit(AssistantEffect.OpenCategory(categoryId, month))
                val text = UiText.Res(
                    R.string.assistant_opening_category,
                    persistentListOf(result.value.category.label, month.formatLong()),
                )
                listOf(ChatItem.AssistantText(nextId(), mode, text))
            }
            is AppResult.Failure -> listOf(notice(mode, NoticeKind.NoData, month))
        }

    private fun priceQuestion(mode: AssistantMode): ChatItem {
        awaitingPrice = true
        return ChatItem.PriceQuestion(nextId(), mode)
    }

    /** Prototype toDetails: a fresh, empty form for [price]. */
    private fun details(mode: AssistantMode, price: Money): ChatItem {
        awaitingPrice = false
        val form = AffordabilityFormUi(price, "", "", availableMissing = false, monthlyMissing = false, submitted = false)
        return ChatItem.AffordabilityDetails(nextId(), mode, form)
    }

    private suspend fun budget(mode: AssistantMode): List<ChatItem> {
        val month = currentMonth()
        return when (val result = finance.budgetDraft(month)) {
            is AppResult.Success -> listOf(ChatItem.Budget(nextId(), mode, result.value.toUi(BudgetStatus.Draft)))
            is AppResult.Failure -> listOf(notice(mode, NoticeKind.NoData, month))
        }
    }

    private fun notice(mode: AssistantMode, kind: NoticeKind, month: YearMonth? = null): ChatItem =
        ChatItem.Notice(nextId(), mode, kind, month)

    private fun suggestion(s: Suggestion) {
        if (currentState.busy) return
        when (s) {
            is Suggestion.Ask -> ask(s.prompt)
            is Suggestion.OpenCategory -> emit(AssistantEffect.OpenCategory(s.categoryId, s.month))
            Suggestion.TrySaving15k -> {
                val latest = currentState.items.filterIsInstance<ChatItem.AffordabilityResult>().lastOrNull() ?: return
                editResult(latest.id) { recalculate(monthly = Money.kes(TrySavingMonthly)) }
            }
        }
    }

    // ---- Affordability ----

    /** Prototype submitForm: flags empty fields, or locks the form and answers with the estimate. */
    private fun submitForm(id: Long) {
        val details = findItem<ChatItem.AffordabilityDetails>(id) ?: return
        val form = details.form
        if (form.submitted) return
        if (form.available.isEmpty() || form.monthly.isEmpty()) {
            editForm(id) { copy(availableMissing = available.isEmpty(), monthlyMissing = monthly.isEmpty()) }
            return
        }
        val available = Money.kes(form.available.toShillings())
        val monthly = Money.kes(form.monthly.toShillings())
        editForm(id) { copy(availableMissing = false, monthlyMissing = false, submitted = true) }
        append(
            ChatItem.User(
                nextId(),
                UiText.Res(R.string.assistant_afford_details_summary, persistentListOf(available.formatKes(), monthly.formatKes())),
            ),
        )
        append(ChatItem.AffordabilityResult(nextId(), details.mode, affordability(form.price, available, monthly)))
    }

    private fun affordability(price: Money, available: Money, monthly: Money): AffordabilityUi {
        val outcome = AffordabilityCalculator.estimate(price, available, monthly, today())
        val months = when (outcome) {
            is AffordabilityOutcome.OnTrack -> outcome.months
            AffordabilityOutcome.Covered, is AffordabilityOutcome.NoEndDate -> 0
        }
        return AffordabilityUi(
            price = price,
            available = available,
            monthly = monthly,
            outcome = outcome,
            percentSetAside = AffordabilityCalculator.percentSetAside(price, available),
            monthBlocks = monthBlocks(currentMonth(), months),
        )
    }

    private fun AffordabilityUi.recalculate(available: Money = this.available, monthly: Money = this.monthly) =
        affordability(price, available, monthly)

    /** Field edits apply only while the form is open. */
    private fun editForm(id: Long, change: AffordabilityFormUi.() -> AffordabilityFormUi) =
        editItem<ChatItem.AffordabilityDetails>(id) { if (form.submitted) this else copy(form = form.change()) }

    private fun editResult(id: Long, change: AffordabilityUi.() -> AffordabilityUi) =
        editItem<ChatItem.AffordabilityResult>(id) { copy(ui = ui.change()) }

    // ---- Budget ----

    /**
     * Edits apply only to a draft and never while busy, so a budget being saved can't change under the save. Totals
     * are recomputed after every change.
     */
    private fun editBudget(id: Long, change: BudgetUi.() -> BudgetUi) {
        if (currentState.busy) return
        editItem<ChatItem.Budget>(id) {
            if (ui.status != BudgetStatus.Draft) {
                this
            } else {
                val edited = ui.change()
                copy(ui = edited.copy(totals = BudgetCalculator.totals(edited.income, edited.lines.map { it.limit })))
            }
        }
    }

    private fun BudgetUi.withLine(index: Int, change: (Money) -> Money): BudgetUi {
        if (index !in lines.indices) return this
        return copy(lines = lines.mapIndexed { i, line -> if (i == index) line.copy(limit = change(line.limit)) else line }.toImmutableList())
    }

    /** Ignored while busy: a budget being saved can't also be discarded. */
    private fun cancelBudget(id: Long) {
        if (currentState.busy) return
        val budget = findItem<ChatItem.Budget>(id)?.takeIf { it.ui.status == BudgetStatus.Draft } ?: return
        editItem<ChatItem.Budget>(id) { copy(ui = ui.copy(status = BudgetStatus.Discarded)) }
        append(ChatItem.AssistantText(nextId(), budget.mode, UiText.Res(R.string.assistant_budget_discarded), afterCancel = true))
    }

    /** Opens the review dialog for a draft; ignored while busy, so a budget being saved can't be confirmed again. */
    private fun reviewBudget(id: Long) {
        if (currentState.busy) return
        if (findItem<ChatItem.Budget>(id)?.ui?.status != BudgetStatus.Draft) return
        reduce { copy(dialog = AssistantDialog.ReviewBudget(id)) }
    }

    /**
     * Prototype confirmSave. Saves the budget as it stood when confirmed, at most once: a second confirmation while
     * saving is ignored, and once saved the budget is no longer a draft. On success the card shows exactly what was
     * written.
     */
    private fun confirmSave() {
        if (currentState.busy) return
        val dialog = currentState.dialog as? AssistantDialog.ReviewBudget ?: return
        reduce { copy(dialog = AssistantDialog.None) }
        val budget = findItem<ChatItem.Budget>(dialog.itemId)?.takeIf { it.ui.status == BudgetStatus.Draft } ?: return
        val confirmed = budget.ui
        respond(ThinkingLabel.Saving) { mode ->
            when (finance.saveBudget(confirmed.toDraft())) {
                is AppResult.Success -> {
                    editItem<ChatItem.Budget>(budget.id) { copy(ui = confirmed.copy(status = BudgetStatus.Saved)) }
                    listOf(ChatItem.BudgetSaved(nextId(), mode, confirmed.month, confirmed.savedSummary()))
                }
                is AppResult.Failure -> listOf(notice(mode, NoticeKind.SaveFailed))
            }
        }
    }

    // ---- Notice actions ----

    private fun noticeAction(action: NoticeAction) = when (action) {
        NoticeAction.AskAgain -> lastQuestion?.let(::ask) ?: Unit
        NoticeAction.ShowThisMonth -> ask(Prompt.SpendingThisMonth)
        NoticeAction.SetUpModel -> emit(AssistantEffect.OpenModelSetup(ModelSetupOrigin.Assistant))
        NoticeAction.UseCloud -> switchMode(AssistantMode.Cloud)
        NoticeAction.UseOnDevice -> switchMode(AssistantMode.OnDevice)
        NoticeAction.RetrySave -> {
            val latest = currentState.items.filterIsInstance<ChatItem.Budget>().lastOrNull()
            if (latest != null) reviewBudget(latest.id) else Unit
        }
    }

    /** Cloud needs consent first; consent is read from the repository, never from not-yet-loaded state. */
    private fun switchMode(target: AssistantMode) {
        if (modeJob?.isActive == true) return
        modeJob = viewModelScope.launch {
            when (target) {
                AssistantMode.Cloud -> {
                    if (!settings.settings.first().cloudConsentGiven) {
                        emit(AssistantEffect.OpenCloudConsent)
                        return@launch
                    }
                    settings.setAssistantMode(AssistantMode.Cloud)
                    messages.show(UiText.Res(R.string.assistant_message_switched_cloud))
                }
                AssistantMode.OnDevice -> {
                    settings.setAssistantMode(AssistantMode.OnDevice)
                    messages.show(UiText.Res(R.string.assistant_message_switched_on_device))
                }
            }
        }
    }

    // ---- Helpers ----

    private fun nextId(): Long = ++lastItemId

    private fun append(item: ChatItem) = reduce { copy(items = (items + item).toImmutableList()) }

    private inline fun <reified T : ChatItem> findItem(id: Long): T? = currentState.items.firstOrNull { it.id == id } as? T

    private inline fun <reified T : ChatItem> editItem(id: Long, crossinline change: T.() -> T) = reduce {
        copy(items = items.map { if (it.id == id && it is T) it.change() else it }.toImmutableList())
    }

    private fun today(): LocalDate = clock.today(timeZone)

    private fun currentMonth(): YearMonth = clock.currentMonth(timeZone)
}

private fun Clock.today(timeZone: TimeZone): LocalDate = now().toLocalDateTime(timeZone).date

private fun Clock.currentMonth(timeZone: TimeZone): YearMonth = today(timeZone).let { YearMonth(it.year, it.month) }

private fun AssistantMode.thinkingLabel(): ThinkingLabel = when (this) {
    AssistantMode.Cloud -> ThinkingLabel.AskingCloud
    AssistantMode.OnDevice -> ThinkingLabel.InterpretingOnDevice
}

private fun InterpretFailure.toNoticeKind(): NoticeKind = when (this) {
    InterpretFailure.CloudUnreachable -> NoticeKind.CloudOffline
    InterpretFailure.ModelUnavailable -> NoticeKind.ModelUnavailable
    InterpretFailure.InvalidOutput -> NoticeKind.InvalidResponse
}

/** ±[shillings], never below zero. */
private fun Money.step(dir: StepDirection, shillings: Long): Money = when (dir) {
    StepDirection.Down -> this - Money.kes(shillings)
    StepDirection.Up -> this + Money.kes(shillings)
}.coerceAtLeast(Money.Zero)

/** ASCII digits only (prototype `digits`), as whole shillings clamped to 0..[MaxShillings]; no digits is zero. */
private fun String.toShillings(): Long {
    val digits = filter { it in '0'..'9' }
    if (digits.isEmpty()) return 0
    return digits.toLongOrNull()?.coerceAtMost(MaxShillings) ?: MaxShillings
}

/** A form field's value: empty while nothing is typed, otherwise the clamped amount without leading zeros. */
private fun String.toAmountDigits(): String = if (none { it in '0'..'9' }) "" else toShillings().toString()
