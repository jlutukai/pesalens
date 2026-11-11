package lutukai.pesalens.feature.assistant.impl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlin.test.assertEquals
import kotlinx.collections.immutable.persistentListOf
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.ui.catalog.model.NoticeKind
import lutukai.pesalens.core.ui.preview.PreviewFixtures
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantContent
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantDialog
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantIntent
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantUiState
import lutukai.pesalens.feature.assistant.impl.presentation.ChatItem
import lutukai.pesalens.feature.assistant.impl.presentation.NoticeAction
import lutukai.pesalens.feature.assistant.impl.presentation.Prompt
import lutukai.pesalens.feature.assistant.impl.presentation.Suggestion
import lutukai.pesalens.feature.assistant.impl.presentation.ThinkingLabel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w412dp-h892dp-420dpi")
class AssistantContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val november = PreviewFixtures.month
    private val intents = mutableListOf<AssistantIntent>()

    private fun show(state: AssistantUiState) {
        compose.setContent { PesaLensTheme(darkTheme = false) { AssistantContent(state, intents::add) } }
    }

    private fun state(
        vararg items: ChatItem,
        mode: AssistantMode = AssistantMode.Cloud,
        draft: String = "",
        dialog: AssistantDialog = AssistantDialog.None,
    ) = AssistantUiState(currentMonth = november, mode = mode, items = persistentListOf(*items), draft = draft, dialog = dialog)

    @Test
    fun emptyConversation_showsGreetingAndThreeIntroChips() {
        show(state())
        compose.onNodeWithText("Hi! I can show where your money went, estimate how long a purchase will take to save for, or draft a budget. What would you like to look at?").assertExists()
        compose.onNodeWithText("Where did my money go this month?").assertExists()
        compose.onNodeWithText("Can I afford a laptop?").assertExists()
        compose.onNodeWithText("Help me create a budget").assertExists()
    }

    @Test
    fun introChip_tap_sendsSuggestion() {
        show(state())
        compose.onNodeWithText("Can I afford a laptop?").performClick()
        assertEquals(
            listOf<AssistantIntent>(
                AssistantIntent.SuggestionTapped(
                    Suggestion.Ask(Prompt.AffordLaptop),
                ),
            ),
            intents,
        )
    }

    @Test
    fun chipsOnlyUnderLastBotItem() {
        show(
            state(
                ChatItem.Notice(1, AssistantMode.Cloud, NoticeKind.Unsupported, null),
                ChatItem.PriceQuestion(2, AssistantMode.Cloud),
            ),
        )
        compose.onNodeWithText("KSh 90,000").assertExists()
        compose.onNodeWithText("KSh 120,000").assertExists()
        // The unsupported notice would offer the intro chips, but it is no longer the last bot item; nor is the greeting.
        compose.onNodeWithText("Where did my money go this month?").assertDoesNotExist()
        compose.onNodeWithText("Can I afford a laptop?").assertDoesNotExist()
    }

    @Test
    fun sendDisabled_whenDraftBlank() {
        show(state(draft = "   "))
        compose.onNodeWithContentDescription("Send").assertIsNotEnabled()
    }

    @Test
    fun sendEnabled_whenDraftHasText_andSendsIntent() {
        show(state(draft = "Can I afford a laptop?"))
        compose.onNodeWithContentDescription("Send").assertIsEnabled().performClick()
        assertEquals(listOf<AssistantIntent>(AssistantIntent.Send), intents)
    }

    @Test
    fun cloudMode_showsCloudPillBannerAndCaption() {
        show(state(ChatItem.AssistantText(1, AssistantMode.Cloud, UiText.Raw("Done.")), mode = AssistantMode.Cloud))
        compose.onNodeWithText("Cloud AI · online").assertExists()
        compose.onNode(hasText("Cloud AI interprets your question.", substring = true)).assertExists()
        compose.onNodeWithText("Cloud AI · figures computed by PesaLens").assertExists()
        compose.onNodeWithText("Sample data").assertExists()
    }

    @Test
    fun onDeviceMode_showsAccentPill() {
        show(state(ChatItem.AssistantText(1, AssistantMode.OnDevice, UiText.Raw("Done.")), mode = AssistantMode.OnDevice))
        compose.onNodeWithText("On-device AI · offline").assertExists()
        compose.onNode(hasText("On-device AI interprets your question.", substring = true)).assertExists()
        compose.onNodeWithText("On-device AI · figures computed by PesaLens").assertExists()
    }

    @Test
    fun categoryRowTap_sendsOpenCategory() {
        show(state(ChatItem.SpendingResult(1, AssistantMode.Cloud, PreviewFixtures.novemberSpending, focus = null)))
        compose.onNodeWithText("Food and groceries").performClick()
        assertEquals(listOf<AssistantIntent>(AssistantIntent.OpenCategory("food", november)), intents)
    }

    @Test
    fun reviewDialog_confirm_sendsConfirmSave() {
        show(
            state(
                ChatItem.Budget(1, AssistantMode.Cloud, PreviewFixtures.budget()),
                dialog = AssistantDialog.ReviewBudget(1),
            ),
        )
        compose.onNodeWithText("Save this budget?").assertExists()
        compose.onNodeWithText("Confirm and save").performClick()
        assertEquals(listOf<AssistantIntent>(AssistantIntent.ConfirmSave), intents)
    }

    @Test
    fun everyChatItemKind_renders() {
        val summary = UiText.Raw("KSh 75,000 budgeted across 6 categories · KSh 25,000 unallocated.")
        val cases: List<Pair<ChatItem, String>> = listOf(
            ChatItem.User(1, UiText.Raw("Where did my money go?")) to "Where did my money go?",
            ChatItem.Thinking(2, ThinkingLabel.Calculating) to "Calculating on this device…",
            ChatItem.AssistantText(3, AssistantMode.Cloud, UiText.Raw("Budget discarded. Nothing was saved.")) to "Budget discarded. Nothing was saved.",
            ChatItem.SpendingResult(4, AssistantMode.Cloud, PreviewFixtures.novemberSpending, null) to "KSh 65,000",
            ChatItem.PriceQuestion(5, AssistantMode.Cloud) to "Happy to estimate that. What's the laptop's price? Type an amount below, or pick an example.",
            ChatItem.AffordabilityDetails(6, AssistantMode.Cloud, PreviewFixtures.affordabilityForm()) to "I need two more amounts",
            ChatItem.AffordabilityResult(7, AssistantMode.Cloud, PreviewFixtures.affordability()) to "PURCHASE ESTIMATE · LAPTOP",
            ChatItem.Budget(8, AssistantMode.Cloud, PreviewFixtures.budget()) to "Suggested from your sample spending",
            ChatItem.BudgetSaved(9, AssistantMode.Cloud, november, summary) to "Budget saved for November 2026",
            ChatItem.Notice(10, AssistantMode.Cloud, NoticeKind.NoData, november) to "No spending data for November 2026",
            ChatItem.Notice(11, AssistantMode.Cloud, NoticeKind.Unsupported, null) to "I can't help with that yet",
            ChatItem.Notice(12, AssistantMode.Cloud, NoticeKind.ModelUnavailable, null) to "The on-device model isn't ready",
            ChatItem.Notice(13, AssistantMode.Cloud, NoticeKind.CloudOffline, null) to "Cloud AI can't be reached",
            ChatItem.Notice(14, AssistantMode.Cloud, NoticeKind.InvalidResponse, null) to "That response couldn't be used",
            ChatItem.Notice(15, AssistantMode.Cloud, NoticeKind.SaveFailed, null) to "Budget not saved",
        )
        var current by mutableStateOf(cases.first().first)
        compose.setContent {
            PesaLensTheme(darkTheme = false) {
                AssistantContent(AssistantUiState(currentMonth = november, items = persistentListOf(current)), {})
            }
        }
        cases.forEach { (item, text) ->
            current = item
            compose.waitForIdle()
            compose.onNode(hasText(text, substring = true), useUnmergedTree = true).assertExists("missing: $text")
        }
    }

    @Test
    fun noticeAction_sendsNoticeActionTapped() {
        show(state(ChatItem.Notice(1, AssistantMode.Cloud, NoticeKind.NoData, YearMonth(2026, 1))))
        compose.onNodeWithText("Show November 2026").performClick()
        assertEquals(
            listOf<AssistantIntent>(
                AssistantIntent.NoticeActionTapped(NoticeAction.ShowThisMonth),
            ),
            intents,
        )
    }

    @Test
    fun settingsButton_sendsOpenSettings() {
        show(state())
        compose.onNodeWithContentDescription("Settings").performClick()
        assertEquals(listOf<AssistantIntent>(AssistantIntent.OpenSettings), intents)
    }

    @Test
    fun budgetControls_whileBusy_sendNoIntents() {
        show(
            state(ChatItem.Budget(1, AssistantMode.Cloud, PreviewFixtures.budget())).copy(busy = true),
        )
        compose.onNodeWithText("Review budget").performClick()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithContentDescription("Increase Rent by KSh 500").performClick()
        assertEquals(emptyList<AssistantIntent>(), intents)
    }

    @Test
    fun budgetCard_whileBusy_listStillScrolls() {
        val items = (1L..3L).map { ChatItem.Budget(it, AssistantMode.Cloud, PreviewFixtures.budget()) }
        show(state(*items.toTypedArray()).copy(busy = true))
        compose.waitForIdle()
        // The conversation opens at its end, so the greeting is off screen; dragging over a card must scroll back up.
        compose.onNodeWithText("Hi! I can show", substring = true).assertIsNotDisplayed()
        repeat(8) {
            compose.onNode(hasScrollAction()).performTouchInput { swipeDown() }
        }
        compose.onNodeWithText("Hi! I can show", substring = true).assertIsDisplayed()
    }

    @Test
    fun newReply_replacingThinking_scrollsToEnd() {
        val history = (1L..4L).map { ChatItem.Budget(it, AssistantMode.Cloud, PreviewFixtures.budget()) }
        var current by mutableStateOf(state(*history.toTypedArray(), ChatItem.Thinking(5, ThinkingLabel.Calculating)))
        compose.setContent { PesaLensTheme(darkTheme = false) { AssistantContent(current, {}) } }
        compose.waitForIdle()
        // Same size, new last id: the reply replaces the Thinking item.
        current = state(
            *history.toTypedArray(),
            ChatItem.SpendingResult(6, AssistantMode.Cloud, PreviewFixtures.novemberSpending, PreviewFixtures.novemberSpending.categories[1]),
        )
        compose.waitForIdle()
        compose.onNodeWithText("Show food and groceries").assertIsDisplayed()
        compose.onNodeWithText("Cloud AI · figures computed by PesaLens").assertExists()
    }
}
