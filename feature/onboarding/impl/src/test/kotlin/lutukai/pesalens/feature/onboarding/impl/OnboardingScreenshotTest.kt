package lutukai.pesalens.feature.onboarding.impl

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
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

private const val DrawAttempts = 10

/**
 * Writes PNGs under `src/test/screenshots/` when run via `recordRoborazziDebug`. The shots are split over small classes
 * because Robolectric's native renderer returns undrawn frames once a class takes too many of them.
 */
abstract class OnboardingScreenshots {
    @get:Rule
    val compose = createComposeRule()

    /** Robolectric's native renderer sometimes hands back the bare window; an undrawn root has a single colour. */
    private fun hasDrawn(): Boolean {
        val bmp = compose.onRoot().captureToImage().asAndroidBitmap()
        val first = bmp.getPixel(0, 0)
        return (0 until bmp.height step 8).any { y -> (0 until bmp.width step 8).any { x -> bmp.getPixel(x, y) != first } }
    }

    protected fun welcome(name: String, dark: Boolean) =
        capture("${name}_${if (dark) "dark" else "light"}", dark) { WelcomeContent(WelcomeUiState(), {}) }

    /** Stands in for the overlay scene's 22dp padding and surface, so the body is judged against the prototype. */
    @Composable
    private fun Sheet() {
        Box(Modifier.fillMaxWidth().background(PesaLensTheme.colors.surface).padding(22.dp)) {
            CloudConsentContent(onIntent = {})
        }
    }

    protected fun consent(name: String, dark: Boolean) = capture("${name}_${if (dark) "dark" else "light"}", dark) { Sheet() }

    private fun capture(name: String, dark: Boolean, content: @Composable () -> Unit) {
        compose.setContent { PesaLensTheme(darkTheme = dark) { content() } }
        for (attempt in 1..DrawAttempts) {
            compose.waitForIdle()
            if (hasDrawn()) break
            // Nothing invalidates a frame the renderer dropped, so ask for another one.
            compose.runOnUiThread { (compose.onRoot().fetchSemanticsNode().root as? View)?.invalidate() }
        }
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class OnboardingPhoneScreenshotTest : OnboardingScreenshots() {
    @Test fun welcome_light() = welcome("welcome", false)
    @Test fun welcome_dark() = welcome("welcome", true)
    @Test fun consent_light() = consent("consent_sheet", false)
    @Test fun consent_dark() = consent("consent_sheet", true)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.PhoneLandscape)
class OnboardingLandscapeScreenshotTest : OnboardingScreenshots() {
    @Test fun welcome_light() = welcome("welcome_landscape", false)
    @Test fun welcome_dark() = welcome("welcome_landscape", true)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Foldable)
class OnboardingFoldableScreenshotTest : OnboardingScreenshots() {
    @Test fun welcome_light() = welcome("welcome_foldable", false)
    @Test fun welcome_dark() = welcome("welcome_foldable", true)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class OnboardingTabletScreenshotTest : OnboardingScreenshots() {
    @Test fun welcome_light() = welcome("welcome_tablet", false)
    @Test fun welcome_dark() = welcome("welcome_tablet", true)
}
