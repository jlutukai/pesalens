package lutukai.pesalens.feature.assistant.impl.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.ui.catalog.AffordabilityCard
import lutukai.pesalens.core.ui.catalog.AffordabilityDetailsCard
import lutukai.pesalens.core.ui.catalog.AssistantBubble
import lutukai.pesalens.core.ui.catalog.BudgetEditorCard
import lutukai.pesalens.core.ui.catalog.BudgetSavedCard
import lutukai.pesalens.core.ui.catalog.SpendingChartCard
import lutukai.pesalens.core.ui.catalog.StatusNotice
import lutukai.pesalens.core.ui.catalog.ThinkingBubble
import lutukai.pesalens.core.ui.catalog.UserBubble
import lutukai.pesalens.core.ui.catalog.model.NoticeActionUi
import lutukai.pesalens.core.ui.catalog.model.NoticeKind
import lutukai.pesalens.core.ui.format.formatLong
import lutukai.pesalens.core.ui.text.asString
import lutukai.pesalens.core.ui.R as CoreUiR
import lutukai.pesalens.feature.assistant.impl.R

/** Cards stop growing at this width on wide screens. */
private val CardMaxWidth = 600.dp
private const val BusyAlpha = 0.6f

/** One conversation entry; the caption and follow-up chips belong to the caller's group. */
@Composable
fun ChatItemView(
    item: ChatItem,
    busy: Boolean,
    currentMonth: YearMonth,
    modelReady: Boolean,
    onIntent: (AssistantIntent) -> Unit,
) {
    when (item) {
        is ChatItem.User -> UserBubble(item.text.asString())
        is ChatItem.Thinking -> ThinkingBubble(stringResource(item.label.labelRes()))
        is ChatItem.AssistantText -> AssistantBubble(item.text.asString())
        is ChatItem.PriceQuestion -> AssistantBubble(stringResource(R.string.assistant_price_question))
        is ChatItem.SpendingResult -> CardSlot {
            SpendingChartCard(
                item.spending,
                onCategoryClick = { onIntent(AssistantIntent.OpenCategory(it, item.spending.month)) },
            )
        }
        is ChatItem.AffordabilityDetails -> CardSlot {
            AffordabilityDetailsCard(
                form = item.form,
                onAvailableChange = { onIntent(AssistantIntent.FormAvailable(item.id, it)) },
                onMonthlyChange = { onIntent(AssistantIntent.FormMonthly(item.id, it)) },
                onUseSample = { onIntent(AssistantIntent.UseSampleValues(item.id)) },
                onSubmit = { onIntent(AssistantIntent.SubmitForm(item.id)) },
            )
        }
        is ChatItem.AffordabilityResult -> CardSlot {
            AffordabilityCard(
                ui = item.ui,
                onMonthly = { onIntent(AssistantIntent.StepMonthly(item.id, it)) },
                onAvailable = { onIntent(AssistantIntent.StepAvailable(item.id, it)) },
            )
        }
        is ChatItem.Budget -> CardSlot {
            // While busy the ViewModel ignores edits; the card is dimmed and its callbacks do nothing. No overlay, so
            // drags over the card still scroll the list.
            BudgetEditorCard(
                ui = item.ui,
                onIncomeChange = { if (!busy) onIntent(AssistantIntent.IncomeChanged(item.id, it)) },
                onLineChange = { index, digits -> if (!busy) onIntent(AssistantIntent.LineChanged(item.id, index, digits)) },
                onLineStep = { index, dir -> if (!busy) onIntent(AssistantIntent.LineStep(item.id, index, dir)) },
                onCancel = { if (!busy) onIntent(AssistantIntent.CancelBudget(item.id)) },
                onReview = { if (!busy) onIntent(AssistantIntent.ReviewBudget(item.id)) },
                modifier = if (busy) Modifier.alpha(BusyAlpha).semantics { disabled() } else Modifier,
            )
        }
        is ChatItem.BudgetSaved -> CardSlot { BudgetSavedCard(item.month, item.summary.asString()) }
        is ChatItem.Notice -> CardSlot { NoticeCard(item, currentMonth, modelReady, onIntent) }
    }
}

/** Left-aligned slot that caps a card at [CardMaxWidth]. */
@Composable
private fun CardSlot(content: @Composable () -> Unit) {
    Box(Modifier.widthIn(max = CardMaxWidth).fillMaxWidth()) { content() }
}

@Composable
private fun NoticeCard(item: ChatItem.Notice, currentMonth: YearMonth, modelReady: Boolean, onIntent: (AssistantIntent) -> Unit) {
    val title = when (item.kind) {
        NoticeKind.NoData -> stringResource(CoreUiR.string.core_ui_notice_no_data_title, (item.month ?: currentMonth).formatLong())
        NoticeKind.Unsupported -> stringResource(CoreUiR.string.core_ui_notice_unsupported_title)
        NoticeKind.ModelUnavailable -> stringResource(CoreUiR.string.core_ui_notice_model_unavailable_title)
        NoticeKind.CloudOffline -> stringResource(CoreUiR.string.core_ui_notice_offline_title)
        NoticeKind.InvalidResponse -> stringResource(CoreUiR.string.core_ui_notice_invalid_title)
        NoticeKind.SaveFailed -> stringResource(CoreUiR.string.core_ui_notice_save_failed_title)
    }
    val body = stringResource(
        when (item.kind) {
            NoticeKind.NoData -> CoreUiR.string.core_ui_notice_no_data_body
            NoticeKind.Unsupported -> CoreUiR.string.core_ui_notice_unsupported_body
            NoticeKind.ModelUnavailable -> CoreUiR.string.core_ui_notice_model_unavailable_body
            NoticeKind.CloudOffline -> CoreUiR.string.core_ui_notice_offline_body
            NoticeKind.InvalidResponse -> CoreUiR.string.core_ui_notice_invalid_body
            NoticeKind.SaveFailed -> CoreUiR.string.core_ui_notice_save_failed_body
        },
    )
    val actions = noticeActions(item.kind, item.mode, modelReady)
    val month = currentMonth.formatLong()
    val uiActions = actions.map { NoticeActionUi(stringResource(it.labelRes(item.kind), month), it.key()) }
    StatusNotice(
        kind = item.kind,
        title = title,
        body = body,
        actions = uiActions.toImmutableList(),
        onAction = { key -> actions.firstOrNull { it.key() == key }?.let { onIntent(AssistantIntent.NoticeActionTapped(it)) } },
    )
}

private fun NoticeAction.key(): String = when (this) {
    NoticeAction.AskAgain -> "ask_again"
    NoticeAction.ShowThisMonth -> "show_this_month"
    NoticeAction.SetUpModel -> "set_up_model"
    NoticeAction.UseCloud -> "use_cloud"
    NoticeAction.UseOnDevice -> "use_on_device"
    NoticeAction.RetrySave -> "retry_save"
}

private fun NoticeAction.labelRes(kind: NoticeKind): Int = when (this) {
    NoticeAction.AskAgain, NoticeAction.RetrySave -> CoreUiR.string.core_ui_notice_try_again
    NoticeAction.ShowThisMonth -> CoreUiR.string.core_ui_notice_no_data_action
    NoticeAction.SetUpModel -> when (kind) {
        NoticeKind.ModelUnavailable -> CoreUiR.string.core_ui_notice_model_unavailable_action_setup
        NoticeKind.NoData, NoticeKind.Unsupported, NoticeKind.CloudOffline, NoticeKind.InvalidResponse,
        NoticeKind.SaveFailed,
        -> CoreUiR.string.core_ui_notice_set_up_on_device_ai
    }
    NoticeAction.UseCloud -> CoreUiR.string.core_ui_notice_use_cloud_ai
    NoticeAction.UseOnDevice -> CoreUiR.string.core_ui_notice_use_on_device_ai
}

private fun ThinkingLabel.labelRes(): Int = when (this) {
    ThinkingLabel.AskingCloud -> R.string.assistant_thinking_asking_cloud
    ThinkingLabel.InterpretingOnDevice -> R.string.assistant_thinking_interpreting_on_device
    ThinkingLabel.Calculating -> R.string.assistant_thinking_calculating
    ThinkingLabel.Saving -> R.string.assistant_thinking_saving
}
