package lutukai.pesalens.feature.applock.impl

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.runtime.CompositionLocalProvider
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import lutukai.pesalens.core.designsystem.theme.PesaColors
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.navigation.LocalNavigator
import lutukai.pesalens.core.testing.fake.FakeNavigator
import lutukai.pesalens.feature.applock.api.Locked
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.applock.impl.presentation.common.PinDotsTag
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedContent
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedError
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedIntent
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedUiState
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupContent
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupIntent
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupUiState
import lutukai.pesalens.feature.applock.impl.presentation.setup.PinError
import lutukai.pesalens.feature.applock.impl.presentation.setup.PinStep
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w412dp-h892dp-420dpi")
class PinContentTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun setPin(state: AppLockSetupUiState, onIntent: (AppLockSetupIntent) -> Unit = {}) =
        compose.setContent { PesaLensTheme(darkTheme = false) { AppLockSetupContent(state, onIntent) } }

    @Test
    fun pinContent_dotsReflectDigits() {
        setPin(AppLockSetupUiState.EnterPin(PinStep.Create, digits = 2, error = null))
        val dots = compose.onNodeWithTag(PinDotsTag)
        var image = dots.captureToImage().asAndroidBitmap()
        // Robolectric's native renderer sometimes drops the first frame; nothing redraws it unless asked.
        repeat(10) {
            if ((0 until image.width).any { x -> image.getPixel(x, image.height / 2) != image.getPixel(0, 0) }) return@repeat
            compose.runOnUiThread { (compose.onRoot().fetchSemanticsNode().root as? View)?.invalidate() }
            compose.waitForIdle()
            image = dots.captureToImage().asAndroidBitmap()
        }
        val accent = PesaColors.Light.accent.toArgb()
        val density = compose.density.density
        // Dots are 16dp circles on an 18dp gap; sample the middle of each.
        val filled = (0 until 4).map { i -> image.getPixel(((8 + 34 * i) * density).toInt(), image.height / 2) == accent }
        assertEquals(listOf(true, true, false, false), filled)
    }

    @Test
    fun pinContent_errorAnnounced() {
        setPin(AppLockSetupUiState.EnterPin(PinStep.Create, digits = 0, error = PinError.Weak))
        compose.onNodeWithText("That PIN is easy to guess. Choose another.")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    @Test
    fun pinContent_keysAndBack_sendIntents() {
        val intents = mutableListOf<AppLockSetupIntent>()
        setPin(AppLockSetupUiState.EnterPin(PinStep.Confirm, digits = 1, error = null), intents::add)
        compose.onNodeWithText("7").performClick()
        compose.onNodeWithContentDescription("Delete digit").performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(
            listOf(AppLockSetupIntent.Digit('7'), AppLockSetupIntent.Delete, AppLockSetupIntent.Back),
            intents,
        )
    }

    @Test
    fun choose_offersTurnOffOnlyWhenLockIsSet_andSkipLabelFollowsOrigin() {
        val intents = mutableListOf<AppLockSetupIntent>()
        compose.setContent {
            PesaLensTheme(darkTheme = false) {
                AppLockSetupContent(AppLockSetupUiState.Choose(LockMethod.Pin, LockSetupOrigin.Settings), intents::add)
            }
        }
        compose.onNodeWithText("PIN is on now").assertIsDisplayed()
        compose.onNodeWithText("Turn off app lock").performClick()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Fingerprint or face unlock").performClick()
        assertEquals(
            listOf(AppLockSetupIntent.TurnOff, AppLockSetupIntent.Skip, AppLockSetupIntent.ChooseBiometric),
            intents,
        )
    }

    @Test
    fun choose_lockOff_hasNoTurnOffAndNoBackOnOnboarding() {
        compose.setContent {
            PesaLensTheme(darkTheme = false) {
                AppLockSetupContent(AppLockSetupUiState.Choose(LockMethod.None, LockSetupOrigin.Onboarding), {})
            }
        }
        compose.onNodeWithText("Turn off app lock").assertDoesNotExist()
        compose.onNodeWithContentDescription("Back").assertDoesNotExist()
        compose.onNodeWithText("Skip for now").assertIsDisplayed()
    }

    @Test
    fun locked_biometricButton_sendsIntent() {
        val intents = mutableListOf<LockedIntent>()
        compose.setContent { PesaLensTheme(darkTheme = false) { LockedContent(LockedUiState.Biometric(), intents::add) } }
        compose.onNodeWithContentDescription("Unlock with fingerprint or face").performClick()
        assertEquals(listOf<LockedIntent>(LockedIntent.UnlockWithBiometric), intents)
        compose.onNodeWithText("Tap to unlock").assertIsDisplayed()
    }

    @Test
    fun locked_backPress_doesNotNavigate() {
        val navigator = FakeNavigator(Locked)
        compose.setContent {
            CompositionLocalProvider(LocalNavigator provides navigator) {
                PesaLensTheme(darkTheme = false) { LockedContent(LockedUiState.Pin(0, null), {}) }
            }
        }
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
        assertEquals(0, navigator.backCalls)
        assertFalse(compose.activity.isFinishing)
    }

    @Test
    fun locked_cooldown_keypadStaysEnabled() {
        compose.setContent {
            PesaLensTheme(darkTheme = false) { LockedContent(LockedUiState.Pin(0, LockedError.CoolingDown(30)), {}) }
        }
        compose.onNodeWithText("Too many attempts. Try again in 30 seconds.").assertIsDisplayed()
        compose.onNodeWithText("5").assertIsEnabled()
        compose.onNodeWithContentDescription("Delete digit").assertIsEnabled()
    }

    @Test
    fun locked_cooldownOfOneSecond_isSingular() {
        compose.setContent {
            PesaLensTheme(darkTheme = false) { LockedContent(LockedUiState.Pin(0, LockedError.CoolingDown(1)), {}) }
        }
        compose.onNodeWithText("Too many attempts. Try again in 1 second.").assertIsDisplayed()
    }

    @Test
    fun locked_biometricUnavailable_showsErrorAndButtonStaysTappable() {
        val intents = mutableListOf<LockedIntent>()
        compose.setContent {
            PesaLensTheme(darkTheme = false) {
                LockedContent(LockedUiState.Biometric(LockedError.BiometricUnavailable), intents::add)
            }
        }
        compose.onNodeWithText("Fingerprint, face or screen lock isn't available right now. Set one up in your phone's settings, then tap to try again.")
            .assertIsDisplayed()
        compose.onNodeWithContentDescription("Unlock with fingerprint or face").performClick()
        assertEquals(listOf<LockedIntent>(LockedIntent.UnlockWithBiometric), intents)
    }
}
