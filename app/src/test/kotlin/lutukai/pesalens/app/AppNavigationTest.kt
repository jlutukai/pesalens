package lutukai.pesalens.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
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
    fun background_withPin_showsLocked() {
        runBlocking { GlobalContext.get().get<AppLockRepository>().setPin(Pin) }

        backgroundAndReturn()
        backgroundAndReturn()
        composeRule.onNodeWithText("PesaLens is locked").assertIsDisplayed()

        // Locked was pushed once, so unlocking returns to where the user was.
        Pin.forEach { digit -> composeRule.onNodeWithText(digit.toString()).performClick() }
        composeRule.onNodeWithText("Get started").assertIsDisplayed()
    }

    private fun finishOnboarding() {
        composeRule.onNodeWithText("Get started").performClick()
        composeRule.onNodeWithText("Agree and continue").performClick()
        composeRule.onNodeWithText("Skip for now").performClick()
        composeRule.onNodeWithContentDescription(Composer).assertIsDisplayed()
    }

    /** Drives the process lifecycle from whatever state an earlier test left it in: foreground, background, foreground. */
    private fun backgroundAndReturn() {
        val process = ProcessLifecycleOwner.get().lifecycle as LifecycleRegistry
        composeRule.runOnUiThread {
            process.currentState = Lifecycle.State.RESUMED
            process.currentState = Lifecycle.State.CREATED
            process.currentState = Lifecycle.State.RESUMED
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val Composer = "Ask about your money"
        const val SpendingQuestion = "Where did my money go this month?"
        const val Draft = "Can I afford a laptop?"
        const val Pin = "2580"
    }
}
