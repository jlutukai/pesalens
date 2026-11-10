package lutukai.pesalens.app

import android.app.Application
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
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

/** A plain application: these tests hand the gate its dependencies, so the app's Koin graph isn't started. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class AppLockGateTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val process = FakeProcess()
    private val navigator = FakeNavigator(Assistant)

    @Test
    fun launch_withPin_doesNotLock() {
        show(LockMethod.Pin)

        assertEquals(listOf(Assistant), navigator.stack)
    }

    @Test
    fun returnFromBackground_withPin_pushesLockedOnce() {
        show(LockMethod.Pin)

        backgroundAndReturn()
        backgroundAndReturn()

        assertEquals(listOf(Assistant, Locked), navigator.stack)
    }

    @Test
    fun returnFromBackground_withBiometric_locks() {
        show(LockMethod.Biometric)

        backgroundAndReturn()

        assertEquals(listOf(Assistant, Locked), navigator.stack)
    }

    @Test
    fun returnFromBackground_withoutLock_staysPut() {
        show(LockMethod.None)

        backgroundAndReturn()

        assertEquals(listOf(Assistant), navigator.stack)
    }

    @Test
    fun lockMethod_isReadOnReturn_notWhenTheGateStarted() {
        val lock = show(LockMethod.Pin)

        process.registry.currentState = Lifecycle.State.CREATED
        lock.stateFlow.value = AppLockState(method = LockMethod.None)
        process.registry.currentState = Lifecycle.State.RESUMED
        composeRule.waitForIdle()

        assertEquals(listOf(Assistant), navigator.stack)
    }

    private fun show(method: LockMethod): FakeAppLockRepository {
        val lock = FakeAppLockRepository(AppLockState(method = method))
        // The process is already in the foreground when the shell first composes.
        process.registry.currentState = Lifecycle.State.RESUMED
        composeRule.setContent { AppLockGate(navigator, lock, process.lifecycle) }
        composeRule.waitForIdle()
        return lock
    }

    private fun backgroundAndReturn() {
        process.registry.currentState = Lifecycle.State.CREATED
        process.registry.currentState = Lifecycle.State.RESUMED
        composeRule.waitForIdle()
    }

    private class FakeProcess : LifecycleOwner {
        val registry = LifecycleRegistry.createUnsafe(this)
        override val lifecycle: Lifecycle get() = registry
    }
}
