package lutukai.pesalens.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.YearMonth
import lutukai.pesalens.core.domain.ai.IntentInterpreter
import lutukai.pesalens.core.domain.ai.Interpretation
import lutukai.pesalens.core.domain.repository.AppLockRepository
import lutukai.pesalens.core.domain.repository.FinanceRepository
import lutukai.pesalens.core.testing.ScreenshotSizes
import lutukai.pesalens.core.testing.fake.FakeFinanceRepository
import lutukai.pesalens.core.testing.fake.FakeIntentInterpreter
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The whole app, as launched: PesaLensApplication starts Koin and MainActivity hosts the shell. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = ScreenshotSizes.Phone)
class AppNavigationTest {

    /** Outermost, so Koin stops only once the activity is gone; the next test's application starts it again. */
    @get:Rule(order = 0)
    val koin = object : ExternalResource() {
        override fun after() = stopKoin()
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launch_showsWelcome() {
        composeRule.onNodeWithText("Understand your money, one question at a time").assertIsDisplayed()
        composeRule.onNodeWithText("Get started").assertIsDisplayed()
    }

    @Test
    fun getStarted_opensConsentSheet_agree_opensLockSetup_skip_landsOnAssistant() {
        composeRule.onNodeWithText("Get started").performClick()
        composeRule.onNodeWithText("Use cloud AI?").assertIsDisplayed()

        composeRule.onNodeWithText("Agree and continue").performClick()
        composeRule.onNodeWithText("Keep your finances private").assertIsDisplayed()

        composeRule.onNodeWithText("Skip for now").performClick()
        composeRule.onNodeWithContentDescription(Composer).assertIsDisplayed()
        // The lock setup's message reaches the app-wide snackbar host.
        composeRule.onNodeWithText("You can turn on app lock later in Settings.").assertIsDisplayed()
    }

    @Test
    fun recreate_keepsDestinationAndChat() {
        finishOnboarding()
        composeRule.onNodeWithText(SpendingQuestion).performClick()
        composeRule.onNodeWithContentDescription(Composer).performTextInput(Draft)

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText(SpendingQuestion).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(Composer).assertIsDisplayed().assertTextEquals(Draft)
    }

    @Test
    @Config(qualifiers = ScreenshotSizes.Tablet)
    fun tabletWidth_categoryOpensBesideChat() {
        loadKoinModules(
            module {
                single<FinanceRepository> { FakeFinanceRepository() }
                single<IntentInterpreter> {
                    FakeIntentInterpreter().apply { next = Interpretation.Spending(YearMonth(2026, 11)) }
                }
            },
        )
        finishOnboarding()

        composeRule.onNodeWithText(SpendingQuestion).performClick()
        composeRule.onNodeWithText("Show food and groceries").performClick()

        // Both panes at once: on a phone the category would replace the chat.
        composeRule.onNodeWithContentDescription(Composer).assertIsDisplayed()
        // The screen sets this label in capitals.
        composeRule.onNodeWithText("Category total", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = ScreenshotSizes.Foldable)
    fun foldableWidth_categoryOpensBesideChat() {
        loadKoinModules(
            module {
                single<FinanceRepository> { FakeFinanceRepository() }
                single<IntentInterpreter> {
                    FakeIntentInterpreter().apply { next = Interpretation.Spending(YearMonth(2026, 11)) }
                }
            },
        )
        finishOnboarding()
        // The lock-setup snackbar would cover the chips at this height.
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("You can turn on app lock later in Settings.").fetchSemanticsNodes().isEmpty()
        }

        composeRule.onNodeWithText(SpendingQuestion).performClick()
        composeRule.onNodeWithText("Show food and groceries").performClick()

        // Both panes at once: on a phone the category would replace the chat.
        composeRule.onNodeWithContentDescription(Composer).assertIsDisplayed()
        // The screen sets this label in capitals.
        composeRule.onNodeWithText("Category total", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun background_withPin_showsLocked() {
        setPin()

        backgroundAndReturn()
        backgroundAndReturn()
        composeRule.onNodeWithText(LockedTitle).assertIsDisplayed()

        unlockReturnsToWelcome()
    }

    /** The activity is destroyed while the app is away (low memory, "Don't keep activities"); the process survives. */
    @Test
    fun activityRecreatedWhileBackgrounded_withPin_showsLocked() {
        setPin()

        backgroundAndReturn(recreateWhileAway = true)
        composeRule.onNodeWithText(LockedTitle).assertIsDisplayed()

        unlockReturnsToWelcome()
    }

    /** A new activity in a process that has not been to the background, as at launch, owes no lock. */
    @Test
    fun coldStart_withNoPriorStop_doesNotLock() {
        setPin()

        composeRule.runOnUiThread { processLifecycle().currentState = Lifecycle.State.RESUMED }
        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText("Get started").assertIsDisplayed()
        composeRule.onNodeWithText(LockedTitle).assertDoesNotExist()
    }

    private fun finishOnboarding() {
        composeRule.onNodeWithText("Get started").performClick()
        composeRule.onNodeWithText("Agree and continue").performClick()
        composeRule.onNodeWithText("Skip for now").performClick()
        composeRule.onNodeWithContentDescription(Composer).assertIsDisplayed()
    }

    private fun setPin() = runBlocking { GlobalContext.get().get<AppLockRepository>().setPin(Pin) }

    /** Unlocking pops Locked back to Welcome, so exactly one Locked was on the back stack. */
    private fun unlockReturnsToWelcome() {
        Pin.forEach { digit -> composeRule.onNodeWithText(digit.toString()).performClick() }
        composeRule.onNodeWithText("Get started").assertIsDisplayed()
        composeRule.onNodeWithText(LockedTitle).assertDoesNotExist()
    }

    /**
     * Leaves and returns as on a device: the activity stops, then the process (ON_STOP); on return the activity starts,
     * then the process (ON_START). The process state is set outright, so whatever an earlier test left in the
     * process-wide owner doesn't matter.
     */
    private fun backgroundAndReturn(recreateWhileAway: Boolean = false) {
        val scenario = composeRule.activityRule.scenario
        val process = processLifecycle()
        composeRule.runOnUiThread { process.currentState = Lifecycle.State.RESUMED }
        scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.runOnUiThread { process.currentState = Lifecycle.State.CREATED }
        if (recreateWhileAway) scenario.recreate()
        scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.runOnUiThread { process.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()
    }

    private fun processLifecycle() = ProcessLifecycleOwner.get().lifecycle as LifecycleRegistry

    private companion object {
        const val Composer = "Ask about your money"
        const val SpendingQuestion = "Where did my money go this month?"
        const val Draft = "Can I afford a laptop?"
        const val Pin = "2580"
        const val LockedTitle = "PesaLens is locked"
    }
}
