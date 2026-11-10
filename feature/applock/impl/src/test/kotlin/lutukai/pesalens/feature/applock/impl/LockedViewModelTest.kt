package lutukai.pesalens.feature.applock.impl

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import lutukai.pesalens.core.model.AppLockState
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.model.PinCheck
import lutukai.pesalens.core.testing.MainDispatcherRule
import lutukai.pesalens.core.testing.TestClock
import lutukai.pesalens.core.testing.fake.FakeAppLockRepository
import lutukai.pesalens.feature.applock.impl.biometric.BiometricOutcome
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedEffect
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedError
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedIntent
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedUiState
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedViewModel
import org.junit.Rule
import org.junit.Test

class LockedViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val clock = TestClock()
    private val lock = FakeAppLockRepository(AppLockState(method = LockMethod.Pin))

    private fun vm() = LockedViewModel(lock, clock)

    /** The countdown ticks on virtual time but reads the clock, so let both pass or runTest never goes idle. */
    private fun TestScope.endCooldown() {
        clock.advanceBy(60.seconds)
        advanceTimeBy(60_000)
    }

    private fun LockedViewModel.type(pin: String) = pin.forEach { onIntent(LockedIntent.Digit(it)) }

    @Test
    fun locked_wrongPin_showsIncorrect() = runTest {
        lock.nextCheck = PinCheck.Wrong(attemptsBeforeCooldown = 4)
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        vm.type("12")
        assertEquals(LockedUiState.Pin(2, null), vm.state.value)
        vm.type("34")
        assertEquals(LockedUiState.Pin(0, LockedError.Incorrect), vm.state.value)
        assertEquals(listOf("1234"), lock.checkedPins)
    }

    @Test
    fun locked_correct_emitsUnlocked_once() = runTest {
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        vm.effects.test {
            vm.type("2580")
            vm.type("2580")
            assertEquals(LockedEffect.Unlocked, awaitItem())
            expectNoEvents()
        }
        assertEquals(listOf("2580"), lock.checkedPins)
    }

    @Test
    fun locked_coolingDown_ignoresDigits() = runTest {
        lock.nextCheck = PinCheck.CoolingDown(clock.now + 30.seconds)
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        vm.type("1357")
        assertEquals(LockedUiState.Pin(0, LockedError.CoolingDown(30)), vm.state.value)
        clock.advanceBy(10.seconds)
        vm.type("9")
        assertEquals(LockedUiState.Pin(0, LockedError.CoolingDown(20)), vm.state.value)
        assertEquals(listOf("1357"), lock.checkedPins)
        endCooldown()
    }

    @Test
    fun locked_cooldownExpired_acceptsDigitsAgain() = runTest {
        lock.nextCheck = PinCheck.CoolingDown(clock.now + 30.seconds)
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        vm.type("1357")
        clock.advanceBy(31.seconds)
        vm.type("9")
        assertEquals(LockedUiState.Pin(1, null), vm.state.value)
    }

    @Test
    fun locked_load_withActiveCooldown_showsRemainingSeconds() = runTest {
        lock.stateFlow.value = AppLockState(LockMethod.Pin, failedAttempts = 5, cooldownEndsAt = clock.now + 12_500.milliseconds)
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        assertEquals(LockedUiState.Pin(0, LockedError.CoolingDown(13)), vm.state.value)
        endCooldown()
    }

    @Test
    fun locked_load_withStaleCooldown_isNotCoolingDown() = runTest {
        lock.stateFlow.value = AppLockState(LockMethod.Pin, failedAttempts = 5, cooldownEndsAt = clock.now - 5.seconds)
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        assertEquals(LockedUiState.Pin(0, null), vm.state.value)
        vm.type("1")
        assertEquals(LockedUiState.Pin(1, null), vm.state.value)
    }

    @Test
    fun locked_delete_removesLastDigit() = runTest {
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        vm.onIntent(LockedIntent.Delete)
        vm.type("12")
        vm.onIntent(LockedIntent.Delete)
        assertEquals(LockedUiState.Pin(1, null), vm.state.value)
    }

    @Test
    fun locked_biometricMethod_showsBiometricAndPrompts() = runTest {
        lock.stateFlow.value = AppLockState(method = LockMethod.Biometric)
        val vm = vm()
        vm.effects.test {
            vm.onIntent(LockedIntent.Load)
            vm.onIntent(LockedIntent.Load)
            assertEquals(LockedEffect.LaunchBiometricUnlock, awaitItem())
            expectNoEvents()
        }
        assertEquals(LockedUiState.Biometric(), vm.state.value)
    }

    @Test
    fun locked_biometricButton_launchesPromptAgain() = runTest {
        lock.stateFlow.value = AppLockState(method = LockMethod.Biometric)
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        vm.effects.test {
            assertEquals(LockedEffect.LaunchBiometricUnlock, awaitItem()) // the automatic prompt on load
            vm.onIntent(LockedIntent.UnlockWithBiometric)
            assertEquals(LockedEffect.LaunchBiometricUnlock, awaitItem())
        }
    }

    @Test
    fun locked_biometricSucceeded_emitsUnlocked() = runTest {
        lock.stateFlow.value = AppLockState(method = LockMethod.Biometric)
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        vm.effects.test {
            assertEquals(LockedEffect.LaunchBiometricUnlock, awaitItem())
            vm.onIntent(LockedIntent.BiometricResult(BiometricOutcome.Succeeded))
            assertEquals(LockedEffect.Unlocked, awaitItem())
        }
    }

    @Test
    fun locked_biometricCancelled_staysLocked() = runTest {
        lock.stateFlow.value = AppLockState(method = LockMethod.Biometric)
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        vm.effects.test {
            assertEquals(LockedEffect.LaunchBiometricUnlock, awaitItem())
            vm.onIntent(LockedIntent.BiometricResult(BiometricOutcome.Cancelled))
            expectNoEvents()
        }
        assertEquals(LockedUiState.Biometric(), vm.state.value)
    }

    @Test
    fun locked_cooldown_countsDownAndClearsWithoutInput() = runTest {
        lock.nextCheck = PinCheck.CoolingDown(clock.now + 30.seconds)
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        vm.type("1357")
        assertEquals(LockedUiState.Pin(0, LockedError.CoolingDown(30)), vm.state.value)
        clock.advanceBy(10.seconds)
        advanceTimeBy(10_001)
        assertEquals(LockedUiState.Pin(0, LockedError.CoolingDown(20)), vm.state.value)
        clock.advanceBy(21.seconds)
        advanceTimeBy(21_000)
        assertEquals(LockedUiState.Pin(0, null), vm.state.value)
        vm.type("9")
        assertEquals(LockedUiState.Pin(1, null), vm.state.value)
    }

    @Test
    fun locked_biometricNotAvailable_showsUnavailableErrorAndAllowsRetry() = runTest {
        lock.stateFlow.value = AppLockState(method = LockMethod.Biometric)
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        vm.effects.test {
            assertEquals(LockedEffect.LaunchBiometricUnlock, awaitItem())
            vm.onIntent(LockedIntent.BiometricResult(BiometricOutcome.NotAvailable))
            assertEquals(LockedUiState.Biometric(LockedError.BiometricUnavailable), vm.state.value)
            vm.onIntent(LockedIntent.UnlockWithBiometric)
            assertEquals(LockedEffect.LaunchBiometricUnlock, awaitItem())
            vm.onIntent(LockedIntent.BiometricResult(BiometricOutcome.Succeeded))
            assertEquals(LockedEffect.Unlocked, awaitItem())
        }
    }

    @Test
    fun locked_biometricError_showsUnavailableError() = runTest {
        lock.stateFlow.value = AppLockState(method = LockMethod.Biometric)
        val vm = vm()
        vm.onIntent(LockedIntent.Load)
        vm.onIntent(LockedIntent.BiometricResult(BiometricOutcome.Error))
        assertEquals(LockedUiState.Biometric(LockedError.BiometricUnavailable), vm.state.value)
    }
}
