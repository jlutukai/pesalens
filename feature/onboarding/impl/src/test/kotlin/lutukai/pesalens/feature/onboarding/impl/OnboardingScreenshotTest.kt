package lutukai.pesalens.feature.onboarding.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.testing.ScreenshotSizes
import lutukai.pesalens.feature.onboarding.impl.presentation.consent.CloudConsentContent
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeContent
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Writes PNGs under `build/outputs/roborazzi/` when run via `recordRoborazziDebug`. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class OnboardingScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test fun welcome_light() = capture("welcome_light", dark = false) { WelcomeContent(WelcomeUiState(), {}) }

    @Test fun welcome_dark() = capture("welcome_dark", dark = true) { WelcomeContent(WelcomeUiState(), {}) }

    @Test
    @Config(qualifiers = ScreenshotSizes.Tablet)
    fun welcome_expanded_light() = capture("welcome_expanded_light", dark = false) { WelcomeContent(WelcomeUiState(), {}) }

    @Test fun consent_sheet_light() = capture("consent_sheet_light", dark = false) { Sheet() }

    @Test fun consent_sheet_dark() = capture("consent_sheet_dark", dark = true) { Sheet() }

    /** Stands in for the overlay scene's 22dp padding and surface, so the body is judged against the prototype. */
    @Composable
    private fun Sheet() {
        Box(Modifier.fillMaxWidth().background(PesaLensTheme.colors.surface).padding(22.dp)) {
            CloudConsentContent(onIntent = {})
        }
    }

    private fun capture(name: String, dark: Boolean, content: @Composable () -> Unit) {
        compose.setContent { PesaLensTheme(darkTheme = dark) { content() } }
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }
}
