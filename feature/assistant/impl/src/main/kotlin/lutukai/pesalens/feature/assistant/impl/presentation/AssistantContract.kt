package lutukai.pesalens.feature.assistant.impl.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.ui.catalog.model.AffordabilityFormUi
import lutukai.pesalens.core.ui.catalog.model.AffordabilityUi
import lutukai.pesalens.core.ui.catalog.model.BudgetUi
import lutukai.pesalens.core.ui.catalog.model.CategorySpendUi
import lutukai.pesalens.core.ui.catalog.model.NoticeKind
import lutukai.pesalens.core.ui.catalog.model.SpendingUi
import lutukai.pesalens.core.ui.catalog.model.StepDirection
import lutukai.pesalens.core.ui.mvi.UiEffect
import lutukai.pesalens.core.ui.mvi.UiIntent
import lutukai.pesalens.core.ui.mvi.UiState
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.assistant.impl.R
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin

@Immutable
data class AssistantUiState(
    val mode: AssistantMode = AssistantMode.Cloud,
    val modelReady: Boolean = false,
    val items: ImmutableList<ChatItem> = persistentListOf(),
    val draft: String = "",
    /** A question or save is in flight; further questions and saves are ignored until it finishes. */
    val busy: Boolean = false,
    val dialog: AssistantDialog = AssistantDialog.None,
) : UiState

/** One conversation entry. [id] is unique for the life of the ViewModel and never reused. */
@Immutable
sealed interface ChatItem {
    val id: Long

    data class User(override val id: Long, val text: UiText) : ChatItem

    data class Thinking(override val id: Long, val label: ThinkingLabel) : ChatItem

    /** [afterCancel] marks the reply to a discarded budget, which offers to start a new one. */
    data class AssistantText(
        override val id: Long,
        val mode: AssistantMode,
        val text: UiText,
        val afterCancel: Boolean = false,
    ) : ChatItem

    data class SpendingResult(
        override val id: Long,
        val mode: AssistantMode,
        val spending: SpendingUi,
        val focus: CategorySpendUi?,
    ) : ChatItem

    data class PriceQuestion(override val id: Long, val mode: AssistantMode) : ChatItem

    data class AffordabilityDetails(override val id: Long, val mode: AssistantMode, val form: AffordabilityFormUi) : ChatItem

    data class AffordabilityResult(override val id: Long, val mode: AssistantMode, val ui: AffordabilityUi) : ChatItem

    data class Budget(override val id: Long, val mode: AssistantMode, val ui: BudgetUi) : ChatItem

    data class BudgetSaved(override val id: Long, val mode: AssistantMode, val month: YearMonth, val summary: UiText) : ChatItem

    /** [month] is the month that had no data, for [NoticeKind.NoData]; null for the other kinds. */
    data class Notice(override val id: Long, val mode: AssistantMode, val kind: NoticeKind, val month: YearMonth?) : ChatItem
}

enum class ThinkingLabel { AskingCloud, InterpretingOnDevice, Calculating, Saving }

@Immutable
sealed interface AssistantDialog {
    data object None : AssistantDialog

    data class ReviewBudget(val itemId: Long) : AssistantDialog
}

/**
 * A canned question. [label] is what the chip and the user bubble show; [query] is the canonical English text sent
 * to the interpreter, independent of the display locale.
 */
enum class Prompt(@StringRes val label: Int, val query: String) {
    SpendingThisMonth(R.string.assistant_prompt_spending_this_month, "Where did my money go this month?"),
    AffordLaptop(R.string.assistant_prompt_afford_laptop, "Can I afford a laptop?"),
    CreateBudget(R.string.assistant_prompt_create_budget, "Help me create a budget"),
    Price90k(R.string.assistant_prompt_price_90k, "KSh 90,000"),
    Price120k(R.string.assistant_prompt_price_120k, "KSh 120,000"),
}

@Immutable
sealed interface Suggestion {
    data class Ask(val prompt: Prompt) : Suggestion

    data class OpenCategory(val categoryId: String, val label: String) : Suggestion

    data object TrySaving15k : Suggestion
}

@Immutable
sealed interface NoticeAction {
    data object AskAgain : NoticeAction

    data object ShowThisMonth : NoticeAction

    data object SetUpModel : NoticeAction

    data object UseCloud : NoticeAction

    data object UseOnDevice : NoticeAction

    data object RetrySave : NoticeAction
}

/** Item-scoped intents carry the [ChatItem.id] of the card they edit. */
sealed interface AssistantIntent : UiIntent {
    data object Load : AssistantIntent
    data class DraftChanged(val text: String) : AssistantIntent
    data object Send : AssistantIntent
    data class SuggestionTapped(val s: Suggestion) : AssistantIntent
    data class OpenCategory(val categoryId: String) : AssistantIntent
    data class FormAvailable(val id: Long, val digits: String) : AssistantIntent
    data class FormMonthly(val id: Long, val digits: String) : AssistantIntent
    data class UseSampleValues(val id: Long) : AssistantIntent
    data class SubmitForm(val id: Long) : AssistantIntent
    data class StepMonthly(val id: Long, val dir: StepDirection) : AssistantIntent
    data class StepAvailable(val id: Long, val dir: StepDirection) : AssistantIntent
    data class IncomeChanged(val id: Long, val digits: String) : AssistantIntent
    data class LineChanged(val id: Long, val index: Int, val digits: String) : AssistantIntent
    data class LineStep(val id: Long, val index: Int, val dir: StepDirection) : AssistantIntent
    data class CancelBudget(val id: Long) : AssistantIntent
    data class ReviewBudget(val id: Long) : AssistantIntent
    data object DismissDialog : AssistantIntent
    data object ConfirmSave : AssistantIntent
    data class NoticeActionTapped(val action: NoticeAction) : AssistantIntent
    data object OpenSettings : AssistantIntent
}

sealed interface AssistantEffect : UiEffect {
    data class OpenCategory(val categoryId: String, val month: YearMonth) : AssistantEffect
    data object OpenSettings : AssistantEffect
    data class OpenModelSetup(val origin: ModelSetupOrigin) : AssistantEffect
    data object OpenCloudConsent : AssistantEffect
}
