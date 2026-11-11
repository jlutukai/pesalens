package lutukai.pesalens.app

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import lutukai.pesalens.core.testing.ScreenshotSizes
import lutukai.pesalens.core.testing.assertFullyDisplayed
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

/**
 * The real shell at landscape, foldable and tablet sizes: screens get the room their scene gives them, every PIN key is
 * reachable, and closing a pane closes that pane. Each class writes one adaptive reference via `recordRoborazziDebug`.
 */
abstract class AdaptiveLayoutChecks : AdaptiveScreenshots() {

    /** Settings' own back arrow, told apart from the extra pane's by the "Settings" title beside it. */
    protected val settingsBack = hasContentDescription("Back") and hasAnySibling(hasText("Settings"))

    protected fun openOnboardingLockSetup() {
        composeRule.onNodeWithText("Get started").tap()
        composeRule.onNodeWithText("Agree and continue").tap()
        composeRule.onNodeWithText("Keep your finances private").assertIsDisplayed()
    }

    protected fun openSettings() {
        composeRule.onNodeWithContentDescription("Settings").tap()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    protected fun openPinStep() {
        composeRule.onNodeWithText("Set a 4-digit PIN").tap()
        composeRule.onNodeWithText("Create a 4-digit PIN").assertExists()
    }

    /**
     * Keys 3, 6 and 9 (the right-hand column the side-by-side layout used to clip), Delete and the title are each on
     * screen in full; with [scroll], after scrolling to them.
     */
    protected fun assertPinPadFullyUsable(scroll: Boolean) {
        fun SemanticsNodeInteraction.reach() = if (scroll) performScrollTo() else this
        listOf("1", "3", "6", "9", "0").forEach { key -> composeRule.onNodeWithText(key).reach().assertFullyDisplayed() }
        composeRule.onNodeWithContentDescription("Delete digit").reach().assertFullyDisplayed()
        composeRule.onNodeWithText("Create a 4-digit PIN").reach().assertFullyDisplayed()
    }

    protected fun assertChatWithPlaceholderOnly() {
        composeRule.onAllNodesWithText("Settings").assertCountEquals(0)
        composeRule.onNodeWithText("Check this device").assertDoesNotExist()
        composeRule.onNodeWithText("Keep your finances private").assertDoesNotExist()
        composeRule.onNodeWithContentDescription(Composer).assertIsDisplayed()
        composeRule.onNodeWithText(DetailPlaceholder).assertIsDisplayed()
    }

    protected fun SemanticsNodeInteraction.assertWiderThan(min: Dp) {
        val width = getUnclippedBoundsInRoot().width
        assertTrue(width > min, "width $width is not more than $min")
    }

    protected companion object {
        const val DetailPlaceholder = "Pick a category from a result, or open Settings"
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.PhoneLandscape)
class LandscapeOnboardingPinTest : AdaptiveLayoutChecks() {
    /** The lock setup gets the whole window, so the header and keypad sit side by side with every key on screen. */
    @Test
    fun onboardingPin_everyKeyOnScreen() {
        openOnboardingLockSetup()
        openPinStep()
        assertPinPadFullyUsable(scroll = false)
        capture("adaptive_onboarding_pin_landscape")
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class TabletOnboardingLockTest : AdaptiveLayoutChecks() {
    /** Not a 360dp extra-pane strip beside an empty pane: the choice column is the full 480dp-wide column. */
    @Test
    fun onboardingLockSetup_isFullWindow() {
        openOnboardingLockSetup()
        composeRule.onNodeWithText("Set a 4-digit PIN").assertWiderThan(400.dp)
        capture("adaptive_onboarding_lock_tablet")
        openPinStep()
        assertPinPadFullyUsable(scroll = false)
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Foldable)
class FoldableOnboardingLockTest : AdaptiveLayoutChecks() {
    @Test
    fun onboardingPin_isFullWindow() {
        openOnboardingLockSetup()
        composeRule.onNodeWithText("Set a 4-digit PIN").assertWiderThan(400.dp)
        openPinStep()
        assertPinPadFullyUsable(scroll = false)
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class TabletModelSetupFromWelcomeTest : AdaptiveLayoutChecks() {
    /** Model setup from Welcome has no list pane below it, so it gets the window (its content caps at 640dp). */
    @Test
    fun modelSetupFromWelcome_isFullWindow_andClosesBackToWelcome() {
        composeRule.onNodeWithText("Use on-device AI instead").tap()
        composeRule.onNodeWithText("Check this device").assertIsDisplayed().assertWiderThan(600.dp)
        capture("adaptive_model_setup_welcome_tablet")

        composeRule.onNodeWithContentDescription("Back").tap()
        composeRule.onNodeWithText("Get started").assertIsDisplayed()
        composeRule.onNodeWithText("Check this device").assertDoesNotExist()
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.PhoneLandscape)
class LandscapeSettingsPinTest : AdaptiveLayoutChecks() {
    /** In the 360dp extra pane beside Settings the PIN screen is one column, scrolling, with no key cut off. */
    @Test
    fun settingsAppLockPin_everyKeyReachable() {
        finishOnboarding()
        openSettings()
        composeRule.onNodeWithText("App lock").tap()
        openPinStep()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        // As first shown: the top of the column, with the lower keypad rows below the fold.
        capture("adaptive_settings_pin_landscape")
        assertPinPadFullyUsable(scroll = true)
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class TabletSettingsPinTest : AdaptiveLayoutChecks() {
    @Test
    fun settingsAppLockPin_fitsTheExtraPane() {
        finishOnboarding()
        openSettings()
        composeRule.onNodeWithText("App lock").tap()
        openPinStep()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        assertPinPadFullyUsable(scroll = false)
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class TabletSettingsBackTest : AdaptiveLayoutChecks() {
    /** Settings' back arrow used to pop the model setup pane beside it, then stop working. */
    @Test
    fun settingsBack_withModelSetupOpen_closesBoth() {
        finishOnboarding()
        openSettings()
        composeRule.onNodeWithText("On-device model").tap()
        composeRule.onNodeWithText("Check this device").assertIsDisplayed()

        composeRule.onNode(settingsBack).tap()

        assertChatWithPlaceholderOnly()
        capture("adaptive_settings_extra_back_tablet")
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class TabletSettingsSingleInstanceTest : AdaptiveLayoutChecks() {
    /** With three panes the gear stays visible; tapping it again returns to the one Settings instead of pushing more. */
    @Test
    fun gearTwice_withModelSetupOpen_keepsOneSettings() {
        finishOnboarding()
        openSettings()
        composeRule.onNodeWithText("On-device model").tap()
        composeRule.onNodeWithText("Check this device").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Settings").tap()
        composeRule.onNodeWithContentDescription("Settings").tap()

        composeRule.onAllNodesWithText("Settings").assertCountEquals(1)
        composeRule.onNodeWithText("Check this device").assertDoesNotExist()
        // One Settings on the stack: closing it once leaves the chat.
        composeRule.onNode(settingsBack).tap()
        assertChatWithPlaceholderOnly()
    }

    /** App lock replaces model setup in the extra pane rather than stacking on it, and its Cancel closes only itself. */
    @Test
    fun appLock_replacesModelSetup_andCancelClosesOnlyIt() {
        finishOnboarding()
        openSettings()
        composeRule.onNodeWithText("On-device model").tap()
        composeRule.onNodeWithText("App lock").tap()

        composeRule.onNodeWithText("Keep your finances private").assertIsDisplayed()
        composeRule.onNodeWithText("Check this device").assertDoesNotExist()

        composeRule.onNodeWithText("Cancel").tap()
        composeRule.onNodeWithText("Keep your finances private").assertDoesNotExist()
        composeRule.onNodeWithText("Check this device").assertDoesNotExist()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()

        composeRule.onNode(settingsBack).tap()
        assertChatWithPlaceholderOnly()
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.PhoneLandscape, fontScale = 2f)
class LandscapeLargeFontConsentTest : AdaptiveLayoutChecks() {
    /** At 200% the consent dialog is taller than a landscape phone; its body scrolls, so Agree stays reachable. */
    @Test
    fun consentDialog_agreeReachableByScrolling() {
        composeRule.onNodeWithText("Get started").tap()
        composeRule.onNodeWithText("Use cloud AI?").assertExists()

        composeRule.onNodeWithText("Agree and continue").performScrollTo().assertFullyDisplayed()
        composeRule.onNodeWithText("Not now").performScrollTo().assertFullyDisplayed()

        composeRule.onNodeWithText("Agree and continue").tap()
        composeRule.onNodeWithText("Keep your finances private").assertExists()
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone, fontScale = 2f)
class PhoneLargeFontConsentTest : AdaptiveLayoutChecks() {
    @Test
    fun consentSheet_agreeReachableByScrolling() {
        composeRule.onNodeWithText("Get started").tap()
        composeRule.onNodeWithText("Use cloud AI?").assertExists()
        composeRule.onNodeWithText("Agree and continue").performScrollTo().assertFullyDisplayed()
    }
}
