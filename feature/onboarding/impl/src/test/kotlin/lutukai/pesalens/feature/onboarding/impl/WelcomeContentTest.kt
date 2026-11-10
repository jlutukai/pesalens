package lutukai.pesalens.feature.onboarding.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlin.test.assertEquals
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.feature.onboarding.impl.presentation.consent.CloudConsentContent
import lutukai.pesalens.feature.onboarding.impl.presentation.consent.CloudConsentIntent
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeContent
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeIntent
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w412dp-h892dp-420dpi")
class WelcomeContentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun welcome_getStarted_sendsIntent() {
        val intents = mutableListOf<WelcomeIntent>()
        compose.setContent { PesaLensTheme(darkTheme = false) { WelcomeContent(WelcomeUiState(), intents::add) } }
        compose.onNodeWithText("Get started").performClick()
        compose.onNodeWithText("Use on-device AI instead").performClick()
        assertEquals(listOf(WelcomeIntent.GetStarted, WelcomeIntent.UseOnDeviceInstead), intents)
    }

    @Test
    fun welcome_previewCard_hiddenFromAccessibility() {
        compose.setContent { PesaLensTheme(darkTheme = false) { WelcomeContent(WelcomeUiState(), {}) } }
        compose.onNodeWithText("Understand your money, one question at a time").assertIsDisplayed()
        compose.onNodeWithText("Where did my money go this month?").assertDoesNotExist()
        compose.onNodeWithText("KSh 25,000").assertDoesNotExist()
    }

    @Test
    fun consent_buttons_sendIntents() {
        val intents = mutableListOf<CloudConsentIntent>()
        compose.setContent { PesaLensTheme(darkTheme = false) { CloudConsentContent(intents::add) } }
        compose.onNodeWithText("Not now").performClick()
        compose.onNodeWithText("Agree and continue").performClick()
        assertEquals(listOf(CloudConsentIntent.NotNow, CloudConsentIntent.Agree), intents)
    }
}
