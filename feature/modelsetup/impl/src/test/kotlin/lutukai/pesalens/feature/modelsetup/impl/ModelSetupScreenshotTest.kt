package lutukai.pesalens.feature.modelsetup.impl

import android.view.View
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.testing.ScreenshotSizes
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupContent
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val DrawAttempts = 10

/** A phone tall enough to show the whole screen without scrolling, so a shot compares to the prototype top to bottom. */
private const val TallPhone = "w412dp-h1000dp-420dpi"

private val origin = ModelSetupOrigin.Settings

/**
 * Writes PNGs under `src/test/screenshots/` when run via `recordRoborazziDebug`. The shots are split over small
 * classes because Robolectric's native renderer returned undrawn frames once a class took too many of them.
 */
abstract class ModelSetupScreenshots {
    @get:Rule
    val compose = createComposeRule()

    /** Robolectric's native renderer sometimes hands back the bare window; an undrawn root has a single colour. */
    private fun hasDrawn(): Boolean {
        val bmp = compose.onRoot().captureToImage().asAndroidBitmap()
        val first = bmp.getPixel(0, 0)
        return (0 until bmp.height step 8).any { y -> (0 until bmp.width step 8).any { x -> bmp.getPixel(x, y) != first } }
    }

    protected fun shot(name: String, dark: Boolean, state: ModelSetupUiState) {
        compose.setContent { PesaLensTheme(darkTheme = dark) { ModelSetupContent(state, {}) } }
        for (attempt in 1..DrawAttempts) {
            compose.waitForIdle()
            if (hasDrawn()) break
            // Nothing invalidates a frame the renderer dropped, so ask for another one.
            compose.runOnUiThread { (compose.onRoot().fetchSemanticsNode().root as? View)?.invalidate() }
        }
        compose.onRoot().captureRoboImage("src/test/screenshots/${name}_${if (dark) "dark" else "light"}.png")
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class ModelSetupNotDownloadedScreenshotTest : ModelSetupScreenshots() {
    private val state = ModelSetupUiState.NotDownloaded(origin)

    @Test fun notDownloaded_light() = shot("model_not_downloaded", false, state)
    @Test fun notDownloaded_dark() = shot("model_not_downloaded", true, state)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class ModelSetupReadyToDownloadScreenshotTest : ModelSetupScreenshots() {
    private val state = ModelSetupUiState.ReadyToDownload(origin)

    @Test fun readyToDownload_light() = shot("model_ready_to_download", false, state)
    @Test fun readyToDownload_dark() = shot("model_ready_to_download", true, state)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class ModelSetupDownloadingScreenshotTest : ModelSetupScreenshots() {
    private val state = ModelSetupUiState.Downloading(origin, 37)

    @Test fun downloading_light() = shot("model_downloading", false, state)
    @Test fun downloading_dark() = shot("model_downloading", true, state)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class ModelSetupVerifyingScreenshotTest : ModelSetupScreenshots() {
    private val state = ModelSetupUiState.Verifying(origin)

    @Test fun verifying_light() = shot("model_verifying", false, state)
    @Test fun verifying_dark() = shot("model_verifying", true, state)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class ModelSetupReadyScreenshotTest : ModelSetupScreenshots() {
    private val state = ModelSetupUiState.Ready(origin)

    @Test fun ready_light() = shot("model_ready", false, state)
    @Test fun ready_dark() = shot("model_ready", true, state)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class ModelSetupFailedScreenshotTest : ModelSetupScreenshots() {
    private val state = ModelSetupUiState.Failed(origin, 42)

    @Test fun failed_light() = shot("model_failed", false, state)
    @Test fun failed_dark() = shot("model_failed", true, state)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class ModelSetupUnsupportedScreenshotTest : ModelSetupScreenshots() {
    private val state = ModelSetupUiState.Unsupported(origin)

    @Test fun unsupported_light() = shot("model_unsupported", false, state)
    @Test fun unsupported_dark() = shot("model_unsupported", true, state)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.PhoneLandscape)
class ModelSetupLandscapeScreenshotTest : ModelSetupScreenshots() {
    private val state = ModelSetupUiState.ReadyToDownload(origin)

    @Test fun readyToDownload_light() = shot("model_landscape", false, state)
    @Test fun readyToDownload_dark() = shot("model_landscape", true, state)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Foldable)
class ModelSetupFoldableScreenshotTest : ModelSetupScreenshots() {
    private val state = ModelSetupUiState.ReadyToDownload(origin)

    @Test fun readyToDownload_light() = shot("model_foldable", false, state)
    @Test fun readyToDownload_dark() = shot("model_foldable", true, state)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class ModelSetupTabletScreenshotTest : ModelSetupScreenshots() {
    private val state = ModelSetupUiState.ReadyToDownload(origin)

    @Test fun readyToDownload_light() = shot("model_tablet", false, state)
    @Test fun readyToDownload_dark() = shot("model_tablet", true, state)
}
