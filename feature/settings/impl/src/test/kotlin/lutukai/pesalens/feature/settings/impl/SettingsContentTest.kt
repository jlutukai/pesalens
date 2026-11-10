package lutukai.pesalens.feature.settings.impl

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import kotlin.test.assertEquals
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.feature.settings.impl.presentation.SettingsContent
import lutukai.pesalens.feature.settings.impl.presentation.SettingsDialog
import lutukai.pesalens.feature.settings.impl.presentation.SettingsIntent
import lutukai.pesalens.feature.settings.impl.presentation.SettingsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w412dp-h1400dp-420dpi")
class SettingsContentTest {
    @get:Rule
    val compose = createComposeRule()

    private fun show(state: SettingsUiState, intents: MutableList<SettingsIntent> = mutableListOf()) {
        compose.setContent { PesaLensTheme(darkTheme = false) { SettingsContent(state, intents::add) } }
    }

    private val radios = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)

    @Test
    fun settings_modeRadioSemantics() {
        val intents = mutableListOf<SettingsIntent>()
        show(SettingsUiState(mode = AssistantMode.Cloud, onDeviceEnabled = false), intents)
        // Mode (2) then theme (2) radios: cloud is selected and on-device is disabled until the model is ready.
        val all = compose.onAllNodes(radios)
        all[0].assertIsSelected().assertIsEnabled()
        all[1].assertIsNotSelected().assertIsNotEnabled()
        all[0].performClick()
        assertEquals(listOf<SettingsIntent>(SettingsIntent.SetMode(AssistantMode.Cloud)), intents)
    }

    @Test
    fun settings_onDeviceEnabledWhenReady_andSelectable() {
        val intents = mutableListOf<SettingsIntent>()
        show(SettingsUiState(mode = AssistantMode.Cloud, onDeviceEnabled = true, modelStatus = ModelStatus.Ready), intents)
        compose.onAllNodes(radios)[1].assertIsEnabled().performClick()
        assertEquals(listOf<SettingsIntent>(SettingsIntent.SetMode(AssistantMode.OnDevice)), intents)
        compose.onNodeWithText("Ready on this device").assertIsDisplayed()
    }

    @Test
    fun settings_themeSegmentShowsResolvedValue() {
        val intents = mutableListOf<SettingsIntent>()
        show(SettingsUiState(resolvedDark = true), intents)
        val all = compose.onAllNodes(radios)
        all[2].assertIsNotSelected()
        all[3].assertIsSelected()
        all[2].performClick()
        assertEquals(listOf<SettingsIntent>(SettingsIntent.SetTheme(dark = false)), intents)
    }

    @Test
    fun settings_privacyExpands() {
        show(SettingsUiState())
        val body = "Sample transactions and saved budgets stay in app storage on this phone. There's no account or analytics."
        compose.onNodeWithText(body).assertDoesNotExist()
        compose.onNodeWithText("Privacy and data").performScrollTo().performClick()
        compose.onNodeWithText(body).assertIsDisplayed()
    }

    @Test
    fun settings_rowsSendIntents() {
        val intents = mutableListOf<SettingsIntent>()
        show(SettingsUiState(), intents)
        compose.onNodeWithText("On-device model").performClick()
        compose.onNodeWithText("App lock").performClick()
        compose.onNodeWithText("Clear conversation").performScrollTo().performClick()
        compose.onNodeWithText("Reset sample data").performScrollTo().performClick()
        assertEquals(
            listOf(SettingsIntent.OpenModel, SettingsIntent.OpenAppLock, SettingsIntent.AskClear, SettingsIntent.AskReset),
            intents,
        )
    }

    @Test
    fun resetDialog_showsCopyAndConfirms() {
        val intents = mutableListOf<SettingsIntent>()
        show(SettingsUiState(dialog = SettingsDialog.ResetSampleData), intents)
        compose.onNodeWithText("Reset sample data?").assertIsDisplayed()
        compose.onNodeWithText(
            "This restores the original sample transactions. Saved budgets and any sample-data changes will be permanently deleted, and the conversation will be cleared.",
        ).assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Delete and reset").performClick()
        assertEquals(listOf<SettingsIntent>(SettingsIntent.Dismiss, SettingsIntent.ConfirmReset), intents)
    }

    @Test
    fun clearDialog_showsCopyAndConfirms() {
        val intents = mutableListOf<SettingsIntent>()
        show(SettingsUiState(dialog = SettingsDialog.ClearConversation), intents)
        compose.onNodeWithText("Clear conversation?").assertIsDisplayed()
        compose.onNodeWithText("Messages and results in this session will be removed. Saved budgets aren't affected.").assertIsDisplayed()
        compose.onNodeWithText("Clear").performClick()
        assertEquals(listOf<SettingsIntent>(SettingsIntent.ConfirmClear), intents)
    }
}
