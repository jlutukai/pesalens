package lutukai.pesalens.core.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.model.PinCheck
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.seconds

class SessionAppLockRepositoryTest {
    private val clock = TestClock()
    private val repo = SessionAppLockRepository(clock)

    @Test
    fun setPin_thenVerifyCorrect_returnsCorrect() = runTest {
        repo.setPin("2580")
        assertEquals(PinCheck.Correct, repo.verifyPin("2580"))
        assertEquals(LockMethod.Pin, repo.state.first().method)
    }

    @Test
    fun wrongPin_returnsWrongWithRemaining() = runTest {
        repo.setPin("2580")
        assertEquals(PinCheck.Wrong(4), repo.verifyPin("0000"))
        assertEquals(PinCheck.Wrong(3), repo.verifyPin("0000"))
        assertEquals(2, repo.state.first().failedAttempts)
    }

    @Test
    fun fiveWrong_coolsDownFor30s() = runTest {
        repo.setPin("2580")
        repeat(4) { repo.verifyPin("0000") }
        val until = clock.now + 30.seconds
        assertEquals(PinCheck.CoolingDown(until), repo.verifyPin("0000"))
        assertEquals(until, repo.state.first().cooldownEndsAt)
        assertEquals(PinCheck.CoolingDown(until), repo.verifyPin("2580"))

        clock.advanceBy(31.seconds)
        assertEquals(PinCheck.Correct, repo.verifyPin("2580"))
        val state = repo.state.first()
        assertEquals(0, state.failedAttempts)
        assertEquals(null, state.cooldownEndsAt)
        assertEquals(PinCheck.Wrong(4), repo.verifyPin("0000"))
    }

    @Test
    fun enableBiometric_clearsPinAndSetsMethod() = runTest {
        repo.setPin("2580")
        repo.enableBiometric()
        assertEquals(LockMethod.Biometric, repo.state.first().method)
        assertFalse(repo.verifyPin("2580") == PinCheck.Correct)
    }

    @Test
    fun disable_resetsEverything() = runTest {
        repo.setPin("2580")
        repo.verifyPin("0000")
        repo.disable()
        val state = repo.state.first()
        assertEquals(LockMethod.None, state.method)
        assertEquals(0, state.failedAttempts)
    }

    @Test
    fun pinIsNotStoredInPlainText() = runTest {
        val pin = "2580"
        repo.setPin(pin)
        var type: Class<*>? = repo.javaClass
        while (type != null && type != Any::class.java) {
            for (field in type.declaredFields) {
                field.isAccessible = true
                val value = field.get(repo)
                assertFalse(value is String && value.contains(pin), "field ${field.name} holds the PIN")
                assertFalse(value is CharSequence && value.contains(pin), "field ${field.name} holds the PIN")
                if (value is java.util.concurrent.atomic.AtomicReference<*>) {
                    assertFalse(value.get().toString().contains(pin), "field ${field.name} holds the PIN")
                }
                if (value is kotlinx.coroutines.flow.StateFlow<*>) {
                    assertFalse(value.value.toString().contains(pin), "field ${field.name} holds the PIN")
                }
            }
            type = type.superclass
        }
    }
}
