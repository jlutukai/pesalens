package lutukai.pesalens.feature.applock.impl

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.testing.MainDispatcherRule
import lutukai.pesalens.core.testing.fake.FakeAppLockRepository
import lutukai.pesalens.core.testing.fake.FakeSettingsRepository
import lutukai.pesalens.core.testing.fake.FakeUserMessageSink
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.applock.impl.biometric.BiometricOutcome
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupEffect
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupIntent
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupUiState
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupViewModel
import lutukai.pesalens.feature.applock.impl.presentation.setup.PinError
import lutukai.pesalens.feature.applock.impl.presentation.setup.PinStep
import org.junit.Rule
import org.junit.Test

class AppLockSetupViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val lock = FakeAppLockRepository()
    private val settings = FakeSettingsRepository()
    private val messages = FakeUserMessageSink()

    private fun vm(origin: LockSetupOrigin = LockSetupOrigin.Onboarding) =
        AppLockSetupViewModel(origin, lock, settings, messages)

    private fun AppLockSetupViewModel.type(pin: String) = pin.forEach { onIntent(AppLockSetupIntent.Digit(it)) }

    private fun AppLockSetupViewModel.startPin() {
        onIntent(AppLockSetupIntent.Load)
        onIntent(AppLockSetupIntent.ChoosePin)
    }

    private fun res(id: Int) = UiText.Res(id)

    @Test
    fun load_reflectsCurrentMethod() = runTest {
        lock.stateFlow.value = lock.stateFlow.value.copy(method = LockMethod.Pin)
        val vm = vm(LockSetupOrigin.Settings)
        vm.onIntent(AppLockSetupIntent.Load)
        assertEquals(AppLockSetupUiState.Choose(LockMethod.Pin, LockSetupOrigin.Settings), vm.state.value)
    }

    @Test
    fun weakPin_showsWeakError() = runTest {
        for (weak in listOf("1111", "1234")) {
            val vm = vm()
            vm.startPin()
            vm.type(weak)
            assertEquals(AppLockSetupUiState.EnterPin(PinStep.Create, 0, PinError.Weak), vm.state.value)
        }
    }

    @Test
    fun goodPin_movesToConfirm_andTypingClearsErrors() = runTest {
        val vm = vm()
        vm.startPin()
        vm.type("1111")
        vm.type("25")
        assertEquals(AppLockSetupUiState.EnterPin(PinStep.Create, 2, null), vm.state.value)
        vm.type("80")
        assertEquals(AppLockSetupUiState.EnterPin(PinStep.Confirm, 0, null), vm.state.value)
    }

    @Test
    fun confirmMismatch_returnsToCreateWithError() = runTest {
        val vm = vm()
        vm.startPin()
        vm.type("2580")
        vm.type("2581")
        assertEquals(AppLockSetupUiState.EnterPin(PinStep.Create, 0, PinError.Mismatch), vm.state.value)
        assertTrue(lock.pins.isEmpty())
    }

    @Test
    fun confirmMatch_setsPinMessagesAndFinishes() = runTest {
        val vm = vm()
        vm.startPin()
        vm.effects.test {
            vm.type("2580")
            vm.type("2580")
            assertEquals(AppLockSetupEffect.Finish, awaitItem())
        }
        assertEquals(listOf("2580"), lock.pins)
        assertEquals(listOf(res(R.string.applock_message_pin_set)), messages.shown)
        assertTrue(settings.settings.first().onboardingComplete)
    }

    @Test
    fun confirmMatch_fromSettings_doesNotCompleteOnboarding() = runTest {
        val vm = vm(LockSetupOrigin.Settings)
        vm.startPin()
        vm.effects.test {
            vm.type("2580")
            vm.type("2580")
            assertEquals(AppLockSetupEffect.Finish, awaitItem())
        }
        assertEquals(false, settings.settings.first().onboardingComplete)
    }

    @Test
    fun pinPress_fifthDigit_ignored() = runTest {
        val vm = vm()
        vm.startPin()
        vm.type("2580")
        vm.type("2580")
        vm.type("1")
        assertEquals(AppLockSetupUiState.EnterPin(PinStep.Confirm, 4, null), vm.state.value)
        assertEquals(listOf("2580"), lock.pins)
    }

    @Test
    fun delete_onEmpty_noop() = runTest {
        val vm = vm()
        vm.startPin()
        vm.onIntent(AppLockSetupIntent.Delete)
        assertEquals(AppLockSetupUiState.EnterPin(PinStep.Create, 0, null), vm.state.value)
        vm.type("25")
        vm.onIntent(AppLockSetupIntent.Delete)
        assertEquals(AppLockSetupUiState.EnterPin(PinStep.Create, 1, null), vm.state.value)
    }

    @Test
    fun back_fromPin_returnsToChoose_andFromChooseRespectsOrigin() = runTest {
        val onboarding = vm()
        onboarding.startPin()
        onboarding.onIntent(AppLockSetupIntent.Back)
        assertEquals(AppLockSetupUiState.Choose(LockMethod.None, LockSetupOrigin.Onboarding), onboarding.state.value)
        onboarding.effects.test {
            onboarding.onIntent(AppLockSetupIntent.Back)
            expectNoEvents()
        }
        val settingsVm = vm(LockSetupOrigin.Settings)
        settingsVm.effects.test {
            settingsVm.onIntent(AppLockSetupIntent.Back)
            assertEquals(AppLockSetupEffect.Close, awaitItem())
        }
    }

    @Test
    fun skip_fromOnboarding_showsHintAndFinishes() = runTest {
        val vm = vm()
        vm.effects.test {
            vm.onIntent(AppLockSetupIntent.Skip)
            vm.onIntent(AppLockSetupIntent.Skip)
            assertEquals(AppLockSetupEffect.Finish, awaitItem())
            expectNoEvents()
        }
        assertEquals(listOf(res(R.string.applock_message_skipped)), messages.shown)
        assertTrue(settings.settings.first().onboardingComplete)
    }

    @Test
    fun skip_fromSettings_closesSilently() = runTest {
        val vm = vm(LockSetupOrigin.Settings)
        vm.effects.test {
            vm.onIntent(AppLockSetupIntent.Skip)
            assertEquals(AppLockSetupEffect.Close, awaitItem())
        }
        assertTrue(messages.shown.isEmpty())
    }

    @Test
    fun turnOff_disablesAndFinishes() = runTest {
        val vm = vm(LockSetupOrigin.Settings)
        vm.effects.test {
            vm.onIntent(AppLockSetupIntent.TurnOff)
            assertEquals(AppLockSetupEffect.Finish, awaitItem())
        }
        assertEquals(1, lock.disableCount)
        assertEquals(listOf(res(R.string.applock_message_off)), messages.shown)
    }

    @Test
    fun chooseBiometric_launchesSystemPrompt() = runTest {
        val vm = vm()
        vm.effects.test {
            vm.onIntent(AppLockSetupIntent.ChooseBiometric)
            assertEquals(AppLockSetupEffect.LaunchBiometricSetup, awaitItem())
        }
    }

    @Test
    fun biometricSucceeded_enablesAndFinishes() = runTest {
        val vm = vm()
        vm.effects.test {
            vm.onIntent(AppLockSetupIntent.BiometricResult(BiometricOutcome.Succeeded))
            assertEquals(AppLockSetupEffect.Finish, awaitItem())
        }
        assertEquals(1, lock.biometricEnabledCount)
        assertEquals(listOf(res(R.string.applock_message_biometric_on)), messages.shown)
    }

    @Test
    fun biometricUsePin_goesToCreate() = runTest {
        val vm = vm()
        vm.onIntent(AppLockSetupIntent.BiometricResult(BiometricOutcome.UsePinInstead))
        assertEquals(AppLockSetupUiState.EnterPin(PinStep.Create, 0, null), vm.state.value)
        assertTrue(messages.shown.isEmpty())
    }

    @Test
    fun biometricNotAvailable_goesToCreateWithHint() = runTest {
        val vm = vm()
        vm.onIntent(AppLockSetupIntent.BiometricResult(BiometricOutcome.NotAvailable))
        assertEquals(AppLockSetupUiState.EnterPin(PinStep.Create, 0, null), vm.state.value)
        assertEquals(listOf(res(R.string.applock_message_biometric_unavailable)), messages.shown)
    }

    @Test
    fun biometricCancelled_staysOnChoose() = runTest {
        val vm = vm()
        vm.onIntent(AppLockSetupIntent.BiometricResult(BiometricOutcome.Cancelled))
        assertEquals(AppLockSetupUiState.Choose(LockMethod.None, LockSetupOrigin.Onboarding), vm.state.value)
    }
}
