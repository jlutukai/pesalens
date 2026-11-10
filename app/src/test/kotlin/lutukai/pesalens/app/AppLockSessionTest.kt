package lutukai.pesalens.app

import androidx.lifecycle.Lifecycle
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import lutukai.pesalens.core.model.AppLockState
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.testing.fake.FakeAppLockRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AppLockSessionTest {

    private val process = FakeProcess()
    private val lock = FakeAppLockRepository()
    private val session = AppLockSession(lock, CoroutineScope(UnconfinedTestDispatcher()))

    @BeforeTest
    fun startInForeground() {
        process.registry.currentState = Lifecycle.State.RESUMED
        session.start(process.lifecycle)
    }

    @Test
    fun start_withPin_owesNothing() {
        lock.stateFlow.value = AppLockState(method = LockMethod.Pin)

        assertFalse(session.lockOwed.value)
    }

    @Test
    fun stop_withoutLock_owesNothing() {
        lock.stateFlow.value = AppLockState(method = LockMethod.None)

        process.registry.currentState = Lifecycle.State.CREATED

        assertFalse(session.lockOwed.value)
    }

    @Test
    fun stop_withPin_owesLock() {
        lock.stateFlow.value = AppLockState(method = LockMethod.Pin)

        process.registry.currentState = Lifecycle.State.CREATED

        assertTrue(session.lockOwed.value)
    }

    @Test
    fun stop_withBiometric_owesLock() {
        lock.stateFlow.value = AppLockState(method = LockMethod.Biometric)

        process.registry.currentState = Lifecycle.State.CREATED

        assertTrue(session.lockOwed.value)
    }

    @Test
    fun consume_clearsTheDebt() {
        lock.stateFlow.value = AppLockState(method = LockMethod.Pin)
        process.registry.currentState = Lifecycle.State.CREATED

        session.consume()

        assertFalse(session.lockOwed.value)
    }
}
