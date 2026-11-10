package lutukai.pesalens.feature.modelsetup.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import kotlin.test.assertEquals
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupContent
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupIntent
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w412dp-h1400dp-420dpi")
class ModelSetupContentTest {
    @get:Rule
    val compose = createComposeRule()

    private val origin = ModelSetupOrigin.Settings

    private fun show(state: ModelSetupUiState, intents: MutableList<ModelSetupIntent> = mutableListOf()) {
        compose.setContent { PesaLensTheme(darkTheme = false) { ModelSetupContent(state, intents::add) } }
    }

    @Test
    fun modelSetup_failed_showsAlertWith42Percent() {
        val intents = mutableListOf<ModelSetupIntent>()
        show(ModelSetupUiState.Failed(origin, 42), intents)
        compose.onNodeWithText("Download failed").assertIsDisplayed()
        compose.onNodeWithText("The download stopped at 42% because the connection dropped. The partial file was removed.").assertIsDisplayed()
        compose.onNodeWithText("Retry download").performClick()
        compose.onNodeWithText("Use cloud AI").performClick()
        assertEquals(listOf(ModelSetupIntent.Retry, ModelSetupIntent.UseCloud), intents)
    }

    @Test
    fun modelSetup_notDownloaded_actions() {
        val intents = mutableListOf<ModelSetupIntent>()
        show(ModelSetupUiState.NotDownloaded(origin), intents)
        compose.onNodeWithText("Check this device").performClick()
        compose.onNodeWithText("Use cloud AI instead").performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(listOf(ModelSetupIntent.CheckDevice, ModelSetupIntent.UseCloud, ModelSetupIntent.Back), intents)
    }

    @Test
    fun modelSetup_readyToDownload_actions() {
        val intents = mutableListOf<ModelSetupIntent>()
        show(ModelSetupUiState.ReadyToDownload(origin), intents)
        compose.onNodeWithText("Download model").performScrollTo().performClick()
        compose.onNodeWithText("Not now").performScrollTo().performClick()
        assertEquals(listOf(ModelSetupIntent.Download, ModelSetupIntent.Back), intents)
    }

    @Test
    fun modelSetup_downloading_showsProgressAndCancels() {
        val intents = mutableListOf<ModelSetupIntent>()
        show(ModelSetupUiState.Downloading(origin, 37), intents)
        compose.onNodeWithContentDescription("Model download").assertIsDisplayed()
        compose.onNodeWithText("37%").assertIsDisplayed()
        compose.onNodeWithText("Cancel download").performScrollTo().performClick()
        assertEquals(listOf<ModelSetupIntent>(ModelSetupIntent.Cancel), intents)
    }

    @Test
    fun modelSetup_verifying_hasNoCancel() {
        show(ModelSetupUiState.Verifying(origin))
        compose.onNodeWithText("Checking the file against a trusted checksum…").assertIsDisplayed()
        compose.onNodeWithText("Cancel download").assertDoesNotExist()
    }

    @Test
    fun modelSetup_ready_actions() {
        val intents = mutableListOf<ModelSetupIntent>()
        show(ModelSetupUiState.Ready(origin), intents)
        compose.onNodeWithText("Switch to on-device AI").performClick()
        compose.onNodeWithText("Delete model").performClick()
        assertEquals(listOf(ModelSetupIntent.UseOnDevice, ModelSetupIntent.Delete), intents)
    }

    @Test
    fun modelSetup_unsupported_continuesWithCloud() {
        val intents = mutableListOf<ModelSetupIntent>()
        show(ModelSetupUiState.Unsupported(origin), intents)
        compose.onNodeWithText("Not supported on this device").assertIsDisplayed()
        compose.onNodeWithText("Continue with cloud AI").performClick()
        assertEquals(listOf<ModelSetupIntent>(ModelSetupIntent.UseCloud), intents)
    }
}
