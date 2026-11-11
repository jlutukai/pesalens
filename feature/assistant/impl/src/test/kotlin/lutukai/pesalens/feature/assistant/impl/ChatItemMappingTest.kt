package lutukai.pesalens.feature.assistant.impl

import kotlin.test.assertEquals
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.model.AffordabilityOutcome
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.BudgetTotals
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.testing.SampleFinance
import lutukai.pesalens.core.ui.catalog.model.AffordabilityFormUi
import lutukai.pesalens.core.ui.catalog.model.AffordabilityUi
import lutukai.pesalens.core.ui.catalog.model.BudgetLineUi
import lutukai.pesalens.core.ui.catalog.model.BudgetStatus
import lutukai.pesalens.core.ui.catalog.model.CategorySpendUi
import lutukai.pesalens.core.ui.catalog.model.NoticeKind
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.assistant.impl.presentation.ChatItem
import lutukai.pesalens.feature.assistant.impl.presentation.IntroSuggestions
import lutukai.pesalens.feature.assistant.impl.presentation.NoticeAction
import lutukai.pesalens.feature.assistant.impl.presentation.Prompt
import lutukai.pesalens.feature.assistant.impl.presentation.Suggestion
import lutukai.pesalens.feature.assistant.impl.presentation.ThinkingLabel
import lutukai.pesalens.feature.assistant.impl.presentation.noticeActions
import lutukai.pesalens.feature.assistant.impl.presentation.savedSummary
import lutukai.pesalens.feature.assistant.impl.presentation.suggestionsAfter
import lutukai.pesalens.feature.assistant.impl.presentation.toDraft
import lutukai.pesalens.feature.assistant.impl.presentation.toUi
import org.junit.Test

class ChatItemMappingTest {
    private val november = YearMonth(2026, 11)
    private val cloud = AssistantMode.Cloud

    @Test
    fun spendingSummary_toUi_mapsRangeCategoriesInOrderAndNoInsight() {
        val ui = SampleFinance.november.toUi()
        assertEquals(november, ui.month)
        assertEquals("1–9 Nov", ui.rangeLabel)
        assertEquals(Money.kes(65_000), ui.total)
        assertEquals(listOf("rent", "food", "transport", "utilities", "shopping", "other"), ui.categories.map { it.id })
        assertEquals(CategorySpendUi("food", "Food and groceries", Money.kes(15_000), 23, 1), ui.categories[1])
        assertEquals(listOf(38, 23, 12, 8, 11, 8), ui.categories.map { it.sharePercent })
        assertEquals(null, ui.insight)
    }

    @Test
    fun budgetDraft_toUi_computesTotalsAndKeepsStatus() {
        val ui = SampleFinance.draft.toUi(BudgetStatus.Saved)
        assertEquals(november, ui.month)
        assertEquals(Money.kes(100_000), ui.income)
        assertEquals(BudgetLineUi("rent", "Rent", 0, Money.kes(25_000)), ui.lines[0])
        assertEquals(BudgetLineUi("other", "Other", 5, Money.kes(10_000)), ui.lines[5])
        assertEquals(BudgetTotals(Money.kes(75_000), Money.kes(25_000), false, 0.75f), ui.totals)
        assertEquals(BudgetStatus.Saved, ui.status)
    }

    @Test
    fun budgetUi_toDraft_roundTrips() {
        assertEquals(SampleFinance.draft, SampleFinance.draft.toUi(BudgetStatus.Draft).toDraft())
    }

    @Test
    fun savedSummary_unallocated() {
        val expected = UiText.Plural(R.plurals.assistant_budget_saved_unallocated, 6, persistentListOf("KSh 75,000", 6, "KSh 25,000"))
        assertEquals(expected, SampleFinance.draft.toUi(BudgetStatus.Saved).savedSummary())
    }

    @Test
    fun savedSummary_overIncome() {
        val over = SampleFinance.draft.copy(income = Money.kes(60_000)).toUi(BudgetStatus.Saved)
        val expected = UiText.Plural(R.plurals.assistant_budget_saved_over_income, 6, persistentListOf("KSh 75,000", 6, "KSh 15,000"))
        assertEquals(expected, over.savedSummary())
    }

    @Test
    fun suggestionsAfter_spending_carriesSpendingMonth() {
        val september = YearMonth(2026, 9)
        val spending = SampleFinance.november.copy(month = september).toUi()
        val chips = suggestionsAfter(ChatItem.SpendingResult(1, cloud, spending, spending.categories[1]))
        assertEquals(Suggestion.OpenCategory("food", "Food and groceries", september), chips.first())
    }

    @Test
    fun noticeActions_table() {
        val askAgain = NoticeAction.AskAgain
        val rows: List<Pair<Triple<NoticeKind, AssistantMode, Boolean>, List<NoticeAction>>> = buildList {
            for (mode in AssistantMode.entries) {
                for (ready in listOf(false, true)) {
                    add(Triple(NoticeKind.NoData, mode, ready) to listOf(NoticeAction.ShowThisMonth))
                    add(Triple(NoticeKind.Unsupported, mode, ready) to emptyList())
                    add(Triple(NoticeKind.ModelUnavailable, mode, ready) to listOf(NoticeAction.SetUpModel, NoticeAction.UseCloud))
                    add(Triple(NoticeKind.SaveFailed, mode, ready) to listOf(NoticeAction.RetrySave))
                }
                add(Triple(NoticeKind.CloudOffline, mode, true) to listOf(askAgain, NoticeAction.UseOnDevice))
                add(Triple(NoticeKind.CloudOffline, mode, false) to listOf(askAgain, NoticeAction.SetUpModel))
            }
            add(Triple(NoticeKind.InvalidResponse, AssistantMode.OnDevice, true) to listOf(askAgain, NoticeAction.UseCloud))
            add(Triple(NoticeKind.InvalidResponse, AssistantMode.OnDevice, false) to listOf(askAgain, NoticeAction.UseCloud))
            add(Triple(NoticeKind.InvalidResponse, AssistantMode.Cloud, true) to listOf(askAgain, NoticeAction.UseOnDevice))
            add(Triple(NoticeKind.InvalidResponse, AssistantMode.Cloud, false) to listOf(askAgain, NoticeAction.SetUpModel))
        }
        // Every NoticeKind x mode x modelReady combination appears exactly once.
        assertEquals(NoticeKind.entries.size * AssistantMode.entries.size * 2, rows.map { it.first }.distinct().size)
        assertEquals(rows.size, rows.map { it.first }.distinct().size)
        rows.forEach { (key, expected) ->
            val (kind, mode, ready) = key
            assertEquals(expected, noticeActions(kind, mode, ready), "noticeActions($kind, $mode, modelReady=$ready)")
        }
    }

    @Test
    fun suggestionsAfter_table() {
        val spending = SampleFinance.november.toUi()
        val form = AffordabilityFormUi(Money.kes(90_000), "", "", false, false, false)
        val afford = AffordabilityUi(
            Money.kes(90_000), Money.kes(30_000), Money.kes(10_000),
            AffordabilityOutcome.OnTrack(6, Money.kes(60_000), YearMonth(2027, 5)), 33, persistentListOf(),
        )
        val budget = SampleFinance.draft.toUi(BudgetStatus.Draft)
        val intro = listOf(Prompt.SpendingThisMonth, Prompt.AffordLaptop, Prompt.CreateBudget).map { Suggestion.Ask(it) }
        val none = emptyList<Suggestion>()

        val rows: List<Pair<ChatItem, List<Suggestion>>> = buildList {
            add(
                ChatItem.SpendingResult(1, cloud, spending, spending.categories[1]) to listOf(
                    Suggestion.OpenCategory("food", "Food and groceries", november),
                    Suggestion.Ask(Prompt.CreateBudget),
                    Suggestion.Ask(Prompt.AffordLaptop),
                ),
            )
            add(
                ChatItem.SpendingResult(2, cloud, spending, focus = null) to
                    listOf(Suggestion.Ask(Prompt.CreateBudget), Suggestion.Ask(Prompt.AffordLaptop)),
            )
            add(ChatItem.PriceQuestion(3, cloud) to listOf(Suggestion.Ask(Prompt.Price90k), Suggestion.Ask(Prompt.Price120k)))
            add(ChatItem.AffordabilityResult(4, cloud, afford) to listOf(Suggestion.TrySaving15k, Suggestion.Ask(Prompt.CreateBudget)))
            add(ChatItem.BudgetSaved(5, cloud, november, UiText.Raw("summary")) to listOf(Suggestion.Ask(Prompt.SpendingThisMonth)))
            add(ChatItem.AssistantText(6, cloud, UiText.Raw("Budget discarded."), afterCancel = true) to listOf(Suggestion.Ask(Prompt.CreateBudget)))
            add(ChatItem.AssistantText(7, cloud, UiText.Raw("Opening Food."), afterCancel = false) to none)
            for (kind in NoticeKind.entries) {
                add(ChatItem.Notice(8, cloud, kind, null) to if (kind == NoticeKind.Unsupported) intro else none)
            }
            add(ChatItem.User(9, UiText.Raw("hi")) to none)
            add(ChatItem.Thinking(10, ThinkingLabel.AskingCloud) to none)
            add(ChatItem.AffordabilityDetails(11, cloud, form) to none)
            add(ChatItem.Budget(12, cloud, budget) to none)
        }
        rows.forEach { (item, expected) -> assertEquals(expected, suggestionsAfter(item), "suggestionsAfter($item)") }
        assertEquals<List<Suggestion>>(intro, IntroSuggestions)
    }
}
