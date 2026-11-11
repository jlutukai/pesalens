package lutukai.pesalens.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithContentDescription
import lutukai.pesalens.core.testing.ScreenshotSizes
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The list-detail scaffold used to move focus into the current pane whenever it changed. That focused the chat
 * composer, the only control focusable in touch mode, and the keyboard opened over a landscape phone unasked.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.PhoneLandscape)
class PaneAutoFocusTest : AdaptiveLayoutChecks() {

    @Test
    fun finishingOnboarding_focusesNothing() {
        finishOnboarding()
        composeRule.onNodeWithContentDescription(Composer).assertIsNotFocused()
        composeRule.onAllNodes(isFocused()).assertCountEquals(0)
    }

    @Test
    fun closingSettings_focusesNothing() {
        finishOnboarding()
        openSettings()
        composeRule.onNode(settingsBack).tap()
        assertChatWithPlaceholderOnly()
        composeRule.onNodeWithContentDescription(Composer).assertIsNotFocused()
        composeRule.onAllNodes(isFocused()).assertCountEquals(0)
    }
}
