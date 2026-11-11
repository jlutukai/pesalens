package lutukai.pesalens.feature.applock.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lutukai.pesalens.core.designsystem.theme.PesaLensTheme
import lutukai.pesalens.core.testing.ScreenshotSizes
import lutukai.pesalens.core.testing.assertFullyDisplayed
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedContent
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedUiState
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupContent
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupUiState
import lutukai.pesalens.feature.applock.impl.presentation.setup.PinStep
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

/**
 * The PIN layout picks side by side or one column from its own room, not the window's size class. On a landscape
 * phone the window is compact-height, but the PIN screen beside Settings is a 360dp-wide pane: side by side there cut
 * off the keypad's right-hand column and Delete.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = ScreenshotSizes.PhoneLandscape)
class PinLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    private val create = AppLockSetupUiState.EnterPin(PinStep.Create, digits = 0, error = null)

    private fun show(width: Dp? = null, height: Dp? = null, content: @Composable () -> Unit) = compose.setContent {
        PesaLensTheme(darkTheme = false) {
            if (width != null && height != null) Box(Modifier.size(width, height)) { content() } else content()
        }
    }

    private fun keys(): List<SemanticsNodeInteraction> =
        listOf("1", "3", "6", "9", "0").map { compose.onNodeWithText(it) } + compose.onNodeWithContentDescription("Delete digit")

    @Test
    fun fullLandscapeWindow_sideBySide_everyKeyOnScreen() {
        show { AppLockSetupContent(create, {}) }
        keys().forEach { it.assertFullyDisplayed() }
        val title = compose.onNodeWithText("Create a 4-digit PIN").assertFullyDisplayed().getUnclippedBoundsInRoot()
        val one = compose.onNodeWithText("1").getUnclippedBoundsInRoot()
        assertTrue(title.right <= one.left, "title $title should sit left of the keypad $one")
    }

    @Test
    fun landscapeExtraPane_oneColumn_everyKeyReachable() {
        show(width = 360.dp, height = 412.dp) { AppLockSetupContent(create, {}) }
        keys().forEach { it.performScrollTo().assertFullyDisplayed() }
        val title = compose.onNodeWithText("Create a 4-digit PIN").performScrollTo().assertFullyDisplayed().getUnclippedBoundsInRoot()
        val one = compose.onNodeWithText("1").getUnclippedBoundsInRoot()
        assertTrue(title.bottom <= one.top, "title $title should sit above the keypad $one")
    }

    @Test
    fun wideButShortArea_stillOneColumnWhenTooNarrowForTwoHalves() {
        // 560dp wide: side by side, each half would be narrower than the 284dp keypad.
        show(width = 560.dp, height = 412.dp) { AppLockSetupContent(create, {}) }
        keys().forEach { it.performScrollTo().assertFullyDisplayed() }
    }

    @Test
    fun lockedScreen_inLandscape_everyKeyOnScreen() {
        show { LockedContent(LockedUiState.Pin(digits = 0, error = null), {}) }
        keys().forEach { it.assertFullyDisplayed() }
        compose.onNodeWithText("PesaLens is locked").assertFullyDisplayed()
    }
}
