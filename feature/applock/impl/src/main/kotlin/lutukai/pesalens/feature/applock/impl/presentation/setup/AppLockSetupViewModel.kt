package lutukai.pesalens.feature.applock.impl.presentation.setup

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import lutukai.pesalens.core.domain.calc.PinPolicy
import lutukai.pesalens.core.domain.repository.AppLockRepository
import lutukai.pesalens.core.domain.repository.SettingsRepository
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.ui.message.UserMessageSink
import lutukai.pesalens.core.ui.mvi.MviViewModel
import lutukai.pesalens.core.ui.text.UiText
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.applock.impl.R
import lutukai.pesalens.feature.applock.impl.biometric.BiometricOutcome

private const val PinLength = 4

class AppLockSetupViewModel(
    private val origin: LockSetupOrigin,
    private val lock: AppLockRepository,
    private val settings: SettingsRepository,
    private val messages: UserMessageSink,
) : MviViewModel<AppLockSetupUiState, AppLockSetupIntent, AppLockSetupEffect>(
    AppLockSetupUiState.Choose(LockMethod.None, origin),
) {
    private var loadJob: Job? = null
    private var method = LockMethod.None

    // The typed digits stay here, never in state or logs.
    private var entry = ""
    private var firstPin = ""

    /** Set once a PIN is accepted or the screen is being left; further input is ignored. */
    private var finished = false

    override fun handleIntent(intent: AppLockSetupIntent) = when (intent) {
        AppLockSetupIntent.Load -> load()
        AppLockSetupIntent.ChooseBiometric -> if (currentState is AppLockSetupUiState.Choose && !finished) emit(AppLockSetupEffect.LaunchBiometricSetup) else Unit
        AppLockSetupIntent.ChoosePin -> if (!finished) startPin() else Unit
        is AppLockSetupIntent.Digit -> digit(intent.d)
        AppLockSetupIntent.Delete -> delete()
        AppLockSetupIntent.Back -> back()
        AppLockSetupIntent.Skip -> skip()
        AppLockSetupIntent.TurnOff -> turnOff()
        is AppLockSetupIntent.BiometricResult -> biometricResult(intent.outcome)
    }

    /** Collects the lock state once for the life of the ViewModel. */
    private fun load() {
        if (loadJob != null) return
        loadJob = lock.state
            .onEach { state ->
                method = state.method
                reduce { if (this is AppLockSetupUiState.Choose) copy(currentMethod = state.method) else this }
            }
            .launchIn(viewModelScope)
    }

    private fun startPin() {
        entry = ""
        firstPin = ""
        reduce { AppLockSetupUiState.EnterPin(PinStep.Create, digits = 0, error = null) }
    }

    private fun digit(d: Char) {
        val state = currentState as? AppLockSetupUiState.EnterPin ?: return
        if (finished || entry.length >= PinLength || !d.isDigit()) return
        entry += d
        if (entry.length < PinLength) {
            reduce { AppLockSetupUiState.EnterPin(state.step, entry.length, error = null) }
            return
        }
        when (state.step) {
            PinStep.Create -> evaluateCreate()
            PinStep.Confirm -> evaluateConfirm()
        }
    }

    private fun evaluateCreate() {
        if (PinPolicy.isWeak(entry)) {
            entry = ""
            reduce { AppLockSetupUiState.EnterPin(PinStep.Create, digits = 0, error = PinError.Weak) }
        } else {
            firstPin = entry
            entry = ""
            reduce { AppLockSetupUiState.EnterPin(PinStep.Confirm, digits = 0, error = null) }
        }
    }

    private fun evaluateConfirm() {
        if (entry != firstPin) {
            entry = ""
            firstPin = ""
            reduce { AppLockSetupUiState.EnterPin(PinStep.Create, digits = 0, error = PinError.Mismatch) }
            return
        }
        val pin = entry
        finished = true
        reduce { AppLockSetupUiState.EnterPin(PinStep.Confirm, PinLength, error = null) }
        viewModelScope.launch {
            lock.setPin(pin)
            messages.show(UiText.Res(R.string.applock_message_pin_set))
            finishAndLeave()
        }
    }

    private fun delete() {
        val state = currentState as? AppLockSetupUiState.EnterPin ?: return
        if (finished || entry.isEmpty()) return
        entry = entry.dropLast(1)
        reduce { AppLockSetupUiState.EnterPin(state.step, entry.length, error = null) }
    }

    private fun back() {
        when (currentState) {
            is AppLockSetupUiState.EnterPin -> if (!finished) {
                entry = ""
                firstPin = ""
                reduce { AppLockSetupUiState.Choose(method, origin) }
            }
            is AppLockSetupUiState.Choose -> if (origin == LockSetupOrigin.Settings && !finished) {
                finished = true
                emit(AppLockSetupEffect.Close)
            }
        }
    }

    private fun skip() {
        if (finished) return
        when (origin) {
            LockSetupOrigin.Onboarding -> {
                finished = true
                viewModelScope.launch {
                    messages.show(UiText.Res(R.string.applock_message_skipped))
                    finishAndLeave()
                }
            }
            LockSetupOrigin.Settings -> {
                finished = true
                emit(AppLockSetupEffect.Close)
            }
        }
    }

    private fun turnOff() {
        if (finished) return
        finished = true
        viewModelScope.launch {
            lock.disable()
            messages.show(UiText.Res(R.string.applock_message_off))
            finishAndLeave()
        }
    }

    private fun biometricResult(outcome: BiometricOutcome) {
        if (finished) return
        when (outcome) {
            BiometricOutcome.Succeeded -> {
                finished = true
                viewModelScope.launch {
                    lock.enableBiometric()
                    messages.show(UiText.Res(R.string.applock_message_biometric_on))
                    finishAndLeave()
                }
            }
            BiometricOutcome.UsePinInstead -> startPin()
            BiometricOutcome.NotAvailable -> {
                messages.show(UiText.Res(R.string.applock_message_biometric_unavailable))
                startPin()
            }
            BiometricOutcome.Cancelled, BiometricOutcome.Error -> Unit
        }
    }

    private suspend fun finishAndLeave() {
        if (origin == LockSetupOrigin.Onboarding) settings.completeOnboarding()
        emit(AppLockSetupEffect.Finish)
    }
}
