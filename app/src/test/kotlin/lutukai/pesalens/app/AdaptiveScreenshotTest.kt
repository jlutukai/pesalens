package lutukai.pesalens.app

import android.view.View
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.domain.ai.IntentInterpreter
import lutukai.pesalens.core.domain.ai.Interpretation
import lutukai.pesalens.core.domain.repository.FinanceRepository
import lutukai.pesalens.core.testing.ScreenshotSizes
import lutukai.pesalens.core.testing.fake.FakeFinanceRepository
import lutukai.pesalens.core.testing.fake.FakeIntentInterpreter
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val DrawAttempts = 10

/**
 * The real shell (MainActivity, Koin graph, Nav3 scenes) at the adaptive sizes. Writes PNGs under
 * `src/test/screenshots/` when run via `recordRoborazziDebug`.
 */
abstract class AdaptiveScreenshots {
    /** Outermost, so Koin stops only once the activity is gone; the next test's application starts it again. */
    @get:Rule(order = 0)
    val koin = object : ExternalResource() {
        override fun after() = stopKoin()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    /** Invokes the click action directly: a real press leaves a ripple whose frame varies from run to run. */
    protected fun SemanticsNodeInteraction.tap() = performSemanticsAction(SemanticsActions.OnClick)

    protected fun withSpendingFakes() = loadKoinModules(
        module {
            single<FinanceRepository> { FakeFinanceRepository() }
            single<IntentInterpreter> {
                FakeIntentInterpreter().apply { next = Interpretation.Spending(YearMonth(2026, 11)) }
            }
        },
    )

    protected fun finishOnboarding() {
        composeRule.onNodeWithText("Get started").tap()
        composeRule.onNodeWithText("Agree and continue").tap()
        composeRule.onNodeWithText("Skip for now").tap()
        composeRule.onNodeWithContentDescription(Composer).assertExists()
        // Let the lock-setup snackbar time out; it would cover the chips and the shots.
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("You can turn on app lock later in Settings.").fetchSemanticsNodes().isEmpty()
        }
    }

    protected fun openCategory() {
        composeRule.onNodeWithText(SpendingQuestion).tap()
        composeRule.onNodeWithText("Show food and groceries").tap()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Category total", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** Robolectric's native renderer sometimes hands back the bare window; an undrawn root has a single colour. */
    private fun hasDrawn(root: SemanticsNodeInteraction): Boolean {
        val bmp = root.captureToImage().asAndroidBitmap()
        val first = bmp.getPixel(0, 0)
        return (0 until bmp.height step 8).any { y -> (0 until bmp.width step 8).any { x -> bmp.getPixel(x, y) != first } }
    }

    /** Captures the activity's root, or with [topWindow] the last (overlay) window's root. */
    protected fun capture(name: String, topWindow: Boolean = false) {
        // The composer, the only control focusable in touch mode, takes focus once the assistant shows. Its cursor
        // blinks on a timer that the real-time waits above (the snackbar timeout) shift, so drop focus for the shot.
        composeRule.runOnUiThread { composeRule.activity.window.decorView.findFocus()?.clearFocus() }
        // Let the press ripple and focus highlight of the last click fade out; they would differ run to run.
        repeat(30) {
            composeRule.mainClock.advanceTimeBy(100)
            composeRule.waitForIdle()
        }
        fun root() = if (topWindow) composeRule.onAllNodes(isRoot()).onLast() else composeRule.onAllNodes(isRoot())[0]
        for (attempt in 1..DrawAttempts) {
            composeRule.waitForIdle()
            if (hasDrawn(root())) break
            // Nothing invalidates a frame the renderer dropped, so ask for another one.
            composeRule.runOnUiThread { (root().fetchSemanticsNode().root as? View)?.invalidate() }
        }
        root().captureRoboImage("src/test/screenshots/$name.png")
    }

    protected companion object {
        const val Composer = "Ask about your money"
        const val SpendingQuestion = "Where did my money go this month?"
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Foldable)
class AdaptiveFoldableScreenshotTest : AdaptiveScreenshots() {
    @Test
    fun listDetail_categoryOpen() {
        withSpendingFakes()
        finishOnboarding()
        openCategory()
        capture("adaptive_list_detail_foldable")
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class AdaptiveTabletScreenshotTest : AdaptiveScreenshots() {
    @Test
    fun listDetail_categoryOpen() {
        withSpendingFakes()
        finishOnboarding()
        openCategory()
        capture("adaptive_list_detail_tablet")
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class AdaptiveExtraPaneScreenshotTest : AdaptiveScreenshots() {
    @Test
    fun settings_modelSetupInExtraPane() {
        finishOnboarding()
        composeRule.onNodeWithContentDescription("Settings").tap()
        composeRule.onNodeWithText("On-device model").tap()
        composeRule.onAllNodesWithText("On-device AI").assertCountEquals(2)
        capture("adaptive_extra_pane_tablet")
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Phone)
class AdaptiveConsentSheetScreenshotTest : AdaptiveScreenshots() {
    @Test
    fun consent_sheetOnPhone() {
        composeRule.onNodeWithText("Get started").tap()
        composeRule.onNodeWithText("Use cloud AI?").assertExists()
        capture("adaptive_consent_sheet_phone", topWindow = true)
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = ScreenshotSizes.Tablet)
class AdaptiveConsentDialogScreenshotTest : AdaptiveScreenshots() {
    @Test
    fun consent_dialogOnTablet() {
        composeRule.onNodeWithText("Get started").tap()
        composeRule.onNodeWithText("Use cloud AI?").assertExists()
        capture("adaptive_consent_dialog_tablet", topWindow = true)
    }
}
