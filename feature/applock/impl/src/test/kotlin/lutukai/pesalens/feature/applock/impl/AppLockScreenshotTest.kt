package lutukai.pesalens.feature.applock.impl

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.testing.ScreenshotSizes
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedContent
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedError
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedUiState
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupContent
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupUiState
import lutukai.pesalens.feature.applock.impl.presentation.setup.PinError
import lutukai.pesalens.feature.applock.impl.presentation.setup.PinStep
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val DrawAttempts = 10

/**
 * Writes PNGs under `src/test/screenshots/` when run via `recordRoborazziDebug`.
 *
 * The shots are split over several small classes: Robolectric's native renderer returned undrawn frames for some
 * shots once a single class took more than about six of them.
 */
abstract class AppLockScreenshots {
    @get:Rule
    val compose = createComposeRule()

    protected val chooseOff = AppLockSetupUiState.Choose(LockMethod.None, LockSetupOrigin.Onboarding)
    protected val chooseOn = AppLockSetupUiState.Choose(LockMethod.Pin, LockSetupOrigin.Settings)
    protected val create = AppLockSetupUiState.EnterPin(PinStep.Create, digits = 0, error = null)
    protected val confirm = AppLockSetupUiState.EnterPin(PinStep.Confirm, digits = 2, error = null)
    protected val weak = AppLockSetupUiState.EnterPin(PinStep.Create, digits = 0, error = PinError.Weak)

    protected fun setup(name: String, dark: Boolean, state: AppLockSetupUiState) =
        capture(name, dark) { AppLockSetupContent(state, {}) }

    /** One representative state per sealed screen state at the larger sizes: choose, PIN entry, locked, light and dark. */
    protected fun chooseShot(suffix: String, dark: Boolean) = setup("choose_${suffix}_${theme(dark)}", dark, chooseOff)
    protected fun createShot(suffix: String, dark: Boolean) = setup("create_${suffix}_${theme(dark)}", dark, create)
    protected fun lockedShot(suffix: String, dark: Boolean) =
        locked("locked_pin_${suffix}_${theme(dark)}", dark, LockedUiState.Pin(2, null))

    private fun theme(dark: Boolean) = if (dark) "dark" else "light"

    protected fun locked(name: String, dark: Boolean, state: LockedUiState) =
        capture(name, dark) { LockedContent(state, {}) }

    /** Robolectric's native renderer sometimes hands back the bare window; an undrawn root has a single colour. */
    private fun hasDrawn(): Boolean {
        val bmp = compose.onRoot().captureToImage().asAndroidBitmap()
        val first = bmp.getPixel(0, 0)
        return (0 until bmp.height step 8).any { y -> (0 until bmp.width step 8).any { x -> bmp.getPixel(x, y) != first } }
    }

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
class AppLockChooseScreenshotTest : AppLockScreenshots() {
    @Test fun choose_lockOff_light() = setup("choose_off_light", false, chooseOff)
    @Test fun choose_lockOff_dark() = setup("choose_off_dark", true, chooseOff)
    @Test fun choose_lockOn_light() = setup("choose_on_light", false, chooseOn)
    @Test fun choose_lockOn_dark() = setup("choose_on_dark", true, chooseOn)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class AppLockPinScreenshotTest : AppLockScreenshots() {
    @Test fun create_light() = setup("create_light", false, create)
    @Test fun create_dark() = setup("create_dark", true, create)
    @Test fun confirm_light() = setup("confirm_light", false, confirm)
    @Test fun confirm_dark() = setup("confirm_dark", true, confirm)
    @Test fun weakError_light() = setup("weak_light", false, weak)
    @Test fun weakError_dark() = setup("weak_dark", true, weak)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class AppLockLockedScreenshotTest : AppLockScreenshots() {
    @Test fun lockedPin_light() = locked("locked_pin_light", false, LockedUiState.Pin(2, null))
    @Test fun lockedPin_dark() = locked("locked_pin_dark", true, LockedUiState.Pin(2, null))
    @Test fun lockedPinWrong_light() = locked("locked_pin_wrong_light", false, LockedUiState.Pin(0, LockedError.Incorrect))
    @Test fun lockedPinCooling_light() = locked("locked_pin_cooling_light", false, LockedUiState.Pin(0, LockedError.CoolingDown(30)))
    @Test fun lockedBio_light() = locked("locked_bio_light", false, LockedUiState.Biometric())
    @Test fun lockedBioUnavailable_light() = locked("locked_bio_unavailable_light", false, LockedUiState.Biometric(LockedError.BiometricUnavailable))
    @Test fun lockedBio_dark() = locked("locked_bio_dark", true, LockedUiState.Biometric())
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.PhoneLandscape)
class AppLockLandscapeScreenshotTest : AppLockScreenshots() {
    @Test fun choose_light() = chooseShot("landscape", false)
    @Test fun choose_dark() = chooseShot("landscape", true)
    @Test fun create_light() = createShot("landscape", false)
    @Test fun create_dark() = createShot("landscape", true)
    @Test fun locked_light() = lockedShot("landscape", false)
    @Test fun locked_dark() = lockedShot("landscape", true)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Foldable)
class AppLockFoldableScreenshotTest : AppLockScreenshots() {
    @Test fun choose_light() = chooseShot("foldable", false)
    @Test fun choose_dark() = chooseShot("foldable", true)
    @Test fun create_light() = createShot("foldable", false)
    @Test fun create_dark() = createShot("foldable", true)
    @Test fun locked_light() = lockedShot("foldable", false)
    @Test fun locked_dark() = lockedShot("foldable", true)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class AppLockTabletScreenshotTest : AppLockScreenshots() {
    @Test fun choose_light() = chooseShot("tablet", false)
    @Test fun choose_dark() = chooseShot("tablet", true)
    @Test fun create_light() = createShot("tablet", false)
    @Test fun create_dark() = createShot("tablet", true)
    @Test fun locked_light() = lockedShot("tablet", false)
    @Test fun locked_dark() = lockedShot("tablet", true)
}
