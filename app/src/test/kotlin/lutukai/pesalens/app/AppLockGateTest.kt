package lutukai.pesalens.app

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import lutukai.pesalens.core.model.AppLockState
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.testing.fake.FakeAppLockRepository
import lutukai.pesalens.core.testing.fake.FakeNavigator
import lutukai.pesalens.feature.applock.api.Locked
import lutukai.pesalens.feature.assistant.api.Assistant
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A plain application: these tests hand the gate its session, so the app's Koin graph isn't started. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class AppLockGateTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val process = FakeProcess()
    private val session = AppLockSession(
        FakeAppLockRepository(AppLockState(method = LockMethod.Pin)),
        CoroutineScope(Dispatchers.Main.immediate),
    )
    private val navigator = FakeNavigator(Assistant)

    @BeforeTest
    fun startInForeground() {
        process.registry.currentState = Lifecycle.State.RESUMED
        session.start(process.lifecycle)
    }

    @Test
    fun nothingOwed_doesNotLock() {
        show()

        assertEquals(listOf(Assistant), navigator.stack)
    }

    @Test
    fun lockOwed_pushesLockedOnce_andConsumesIt() {
        show()

        backgroundAndReturn()
        assertEquals(listOf(Assistant, Locked), navigator.stack)
        assertFalse(session.lockOwed.value)

        backgroundAndReturn()
        assertEquals(listOf(Assistant, Locked), navigator.stack)
        assertFalse(session.lockOwed.value)
    }

    /** A gate in an activity created after the app went to the background, as when the old one was destroyed. */
    @Test
    fun lockOwedBeforeTheGateStarted_locks() {
        process.registry.currentState = Lifecycle.State.CREATED
        process.registry.currentState = Lifecycle.State.RESUMED

        show()

        assertEquals(listOf(Assistant, Locked), navigator.stack)
        assertFalse(session.lockOwed.value)
    }

    /** Locked joins the back stack only once the activity is started again, not while it sits stopped. */
    @Test
    fun lockOwedWhileActivityStopped_waitsUntilItStarts() {
        show()
        val scenario = composeRule.activityRule.scenario

        scenario.moveToState(Lifecycle.State.CREATED)
        process.registry.currentState = Lifecycle.State.CREATED
        composeRule.waitForIdle()
        assertEquals(listOf(Assistant), navigator.stack)
        assertTrue(session.lockOwed.value)

        scenario.moveToState(Lifecycle.State.RESUMED)
        process.registry.currentState = Lifecycle.State.RESUMED
        composeRule.waitForIdle()
        assertEquals(listOf(Assistant, Locked), navigator.stack)
        assertFalse(session.lockOwed.value)
    }

    private fun show() {
        composeRule.setContent { AppLockGate(navigator, session) }
        composeRule.waitForIdle()
    }

    private fun backgroundAndReturn() {
        process.registry.currentState = Lifecycle.State.CREATED
        process.registry.currentState = Lifecycle.State.RESUMED
        composeRule.waitForIdle()
    }
}
