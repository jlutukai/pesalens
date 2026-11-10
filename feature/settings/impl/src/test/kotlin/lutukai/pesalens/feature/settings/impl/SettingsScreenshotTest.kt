package lutukai.pesalens.feature.settings.impl

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.model.ModelStatus
import lutukai.pesalens.core.testing.ScreenshotSizes
import lutukai.pesalens.feature.settings.impl.presentation.SettingsContent
import lutukai.pesalens.feature.settings.impl.presentation.SettingsDialog
import lutukai.pesalens.feature.settings.impl.presentation.SettingsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val DrawAttempts = 10

/** A phone tall enough to show the whole screen without scrolling, so a shot compares to the prototype top to bottom. */
private const val TallPhone = "w412dp-h1000dp-420dpi"

/**
 * Writes PNGs under `src/test/screenshots/` when run via `recordRoborazziDebug`. The shots are split over small
 * classes because Robolectric's native renderer returned undrawn frames once a class took too many of them, and a
 * dialog window left over from one test confuses the next test's root capture in the same class.
 */
abstract class SettingsScreenshots {
    @get:Rule
    val compose = createComposeRule()

    protected val state = SettingsUiState(
        mode = AssistantMode.Cloud,
        onDeviceEnabled = false,
        modelStatus = ModelStatus.NotDownloaded,
        lockMethod = LockMethod.None,
    )

    /** Robolectric's native renderer sometimes hands back the bare window; an undrawn root has a single colour. */
    private fun hasDrawn(): Boolean {
        val bmp = compose.onRoot().captureToImage().asAndroidBitmap()
        val first = bmp.getPixel(0, 0)
        return (0 until bmp.height step 8).any { y -> (0 until bmp.width step 8).any { x -> bmp.getPixel(x, y) != first } }
    }

    private fun draw(dark: Boolean, content: @Composable () -> Unit) {
        compose.setContent { PesaLensTheme(darkTheme = dark) { content() } }
        for (attempt in 1..DrawAttempts) {
            compose.waitForIdle()
            if (hasDrawn()) break
            // Nothing invalidates a frame the renderer dropped, so ask for another one.
            compose.runOnUiThread { (compose.onRoot().fetchSemanticsNode().root as? View)?.invalidate() }
        }
    }

    protected fun shot(name: String, dark: Boolean, state: SettingsUiState, fontScale: Float = 1f) {
        draw(dark) {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                SettingsContent(state, {})
            }
        }
        compose.onRoot().captureRoboImage("src/test/screenshots/${name}_${if (dark) "dark" else "light"}.png")
    }

    /** The dialog is its own window and root; capture that one. */
    protected fun dialogShot(name: String, dark: Boolean, state: SettingsUiState) {
        compose.setContent { PesaLensTheme(darkTheme = dark) { SettingsContent(state, {}) } }
        compose.waitForIdle()
        compose.onAllNodes(isRoot()).onLast().captureRoboImage("src/test/screenshots/${name}_${if (dark) "dark" else "light"}.png")
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class SettingsScreenshotTest : SettingsScreenshots() {
    @Test fun settings_light() = shot("settings", false, state)
    @Test fun settings_dark() = shot("settings", true, state)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class SettingsReadyScreenshotTest : SettingsScreenshots() {
    private val ready = state.copy(
        mode = AssistantMode.OnDevice,
        onDeviceEnabled = true,
        modelStatus = ModelStatus.Ready,
        lockMethod = LockMethod.Biometric,
        resolvedDark = true,
    )

    @Test fun settingsOnDevice_light() = shot("settings_on_device", false, ready)
    @Test fun settingsOnDevice_dark() = shot("settings_on_device", true, ready)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TallPhone)
class SettingsFontScaleScreenshotTest : SettingsScreenshots() {
    @Test fun settings_fontScale2_light() = shot("settings_fontscale2", false, state, fontScale = 2f)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class SettingsClearDialogScreenshotTest : SettingsScreenshots() {
    private val clear = state.copy(dialog = SettingsDialog.ClearConversation)

    @Test fun clearDialog_light() = dialogShot("settings_clear_dialog", false, clear)
    @Test fun clearDialog_dark() = dialogShot("settings_clear_dialog", true, clear)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class SettingsResetDialogScreenshotTest : SettingsScreenshots() {
    private val reset = state.copy(dialog = SettingsDialog.ResetSampleData)

    @Test fun resetDialog_light() = dialogShot("settings_reset_dialog", false, reset)
    @Test fun resetDialog_dark() = dialogShot("settings_reset_dialog", true, reset)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.PhoneLandscape)
class SettingsLandscapeScreenshotTest : SettingsScreenshots() {
    @Test fun settings_light() = shot("settings_landscape", false, state)
    @Test fun settings_dark() = shot("settings_landscape", true, state)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Foldable)
class SettingsFoldableScreenshotTest : SettingsScreenshots() {
    @Test fun settings_light() = shot("settings_foldable", false, state)
    @Test fun settings_dark() = shot("settings_foldable", true, state)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class SettingsTabletScreenshotTest : SettingsScreenshots() {
    @Test fun settings_light() = shot("settings_tablet", false, state)
    @Test fun settings_dark() = shot("settings_tablet", true, state)
}
