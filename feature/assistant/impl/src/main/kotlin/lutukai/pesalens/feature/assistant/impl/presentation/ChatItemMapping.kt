package lutukai.pesalens.feature.assistant.impl.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import lutukai.pesalens.core.domain.calc.BudgetCalculator
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.BudgetDraft
import lutukai.pesalens.core.model.BudgetLine
import lutukai.pesalens.core.model.Category
import lutukai.pesalens.core.model.Money
import lutukai.pesalens.core.model.SpendingSummary
import lutukai.pesalens.core.ui.catalog.model.BudgetLineUi
import lutukai.pesalens.core.ui.catalog.model.BudgetStatus
import lutukai.pesalens.core.ui.catalog.model.BudgetUi
import lutukai.pesalens.core.ui.catalog.model.CategorySpendUi
import lutukai.pesalens.core.ui.catalog.model.NoticeKind
import lutukai.pesalens.core.ui.catalog.model.SpendingUi
import lutukai.pesalens.core.ui.format.formatDayMonth
import lutukai.pesalens.core.ui.format.formatKes
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.assistant.impl.R

/** The prototype's SUGGEST: shown on an empty conversation and under the unsupported-question notice. */
val IntroSuggestions: ImmutableList<Suggestion> = persistentListOf(
    Suggestion.Ask(Prompt.SpendingThisMonth),
    Suggestion.Ask(Prompt.AffordLaptop),
    Suggestion.Ask(Prompt.CreateBudget),
)

/** Categories keep the domain's order. The insight stays null until it can be templated per locale. */
fun SpendingSummary.toUi(): SpendingUi = SpendingUi(
    month = month,
    rangeLabel = "1–" + coveredThrough.formatDayMonth(),
    total = total,
    categories = categories.map {
        CategorySpendUi(it.category.id, it.category.label, it.amount, it.sharePercent, it.category.tone)
    }.toImmutableList(),
    insight = null,
)

fun BudgetDraft.toUi(status: BudgetStatus): BudgetUi = BudgetUi(
    month = month,
    income = income,
    lines = lines.map { BudgetLineUi(it.category.id, it.category.label, it.category.tone, it.limit) }.toImmutableList(),
    totals = BudgetCalculator.totals(income, lines.map { it.limit }),
    status = status,
)

/** The draft to save; each line's category is rebuilt from the id, label and tone the card carries. */
internal fun BudgetUi.toDraft(): BudgetDraft = BudgetDraft(
    month = month,
    income = income,
    lines = lines.map { BudgetLine(Category(it.categoryId, it.label, it.tone), it.limit) },
)

/** "KSh 75,000 budgeted across 6 categories · KSh 25,000 unallocated." or "… · KSh 5,000 over income." */
internal fun BudgetUi.savedSummary(): UiText = if (totals.isOver) {
    UiText.Res(
        R.string.assistant_budget_saved_over_income,
        persistentListOf(totals.total.formatKes(), lines.size, (Money.Zero - totals.difference).formatKes()),
    )
} else {
    UiText.Res(
        R.string.assistant_budget_saved_unallocated,
        persistentListOf(totals.total.formatKes(), lines.size, totals.difference.formatKes()),
    )
}

/** Follow-up chips for [item] (prototype renderVals). The screen shows them only under the last non-user item. */
fun suggestionsAfter(item: ChatItem): ImmutableList<Suggestion> = when (item) {
    is ChatItem.SpendingResult -> listOfNotNull(
        item.focus?.let { Suggestion.OpenCategory(it.id, it.label, item.spending.month) },
        Suggestion.Ask(Prompt.CreateBudget),
        Suggestion.Ask(Prompt.AffordLaptop),
    ).toImmutableList()
    is ChatItem.PriceQuestion -> persistentListOf(Suggestion.Ask(Prompt.Price90k), Suggestion.Ask(Prompt.Price120k))
    is ChatItem.AffordabilityResult -> persistentListOf(Suggestion.TrySaving15k, Suggestion.Ask(Prompt.CreateBudget))
    is ChatItem.BudgetSaved -> persistentListOf(Suggestion.Ask(Prompt.SpendingThisMonth))
    is ChatItem.AssistantText -> if (item.afterCancel) persistentListOf(Suggestion.Ask(Prompt.CreateBudget)) else persistentListOf()
    is ChatItem.Notice -> when (item.kind) {
        NoticeKind.Unsupported -> IntroSuggestions
        NoticeKind.NoData, NoticeKind.ModelUnavailable, NoticeKind.CloudOffline, NoticeKind.InvalidResponse,
        NoticeKind.SaveFailed,
        -> persistentListOf()
    }
    is ChatItem.User, is ChatItem.Thinking, is ChatItem.AffordabilityDetails, is ChatItem.Budget -> persistentListOf()
}

/** A notice's buttons (prototype notice(v)); the alternative-mode button depends on [mode] and [modelReady]. */
fun noticeActions(kind: NoticeKind, mode: AssistantMode, modelReady: Boolean): ImmutableList<NoticeAction> {
    val onDeviceOrSetUp = if (modelReady) NoticeAction.UseOnDevice else NoticeAction.SetUpModel
    return when (kind) {
        NoticeKind.NoData -> persistentListOf(NoticeAction.ShowThisMonth)
        NoticeKind.Unsupported -> persistentListOf()
        NoticeKind.ModelUnavailable -> persistentListOf(NoticeAction.SetUpModel, NoticeAction.UseCloud)
        NoticeKind.CloudOffline -> persistentListOf(NoticeAction.AskAgain, onDeviceOrSetUp)
        NoticeKind.InvalidResponse -> persistentListOf(
            NoticeAction.AskAgain,
            when (mode) {
                AssistantMode.OnDevice -> NoticeAction.UseCloud
                AssistantMode.Cloud -> onDeviceOrSetUp
            },
        )
        NoticeKind.SaveFailed -> persistentListOf(NoticeAction.RetrySave)
    }
}
