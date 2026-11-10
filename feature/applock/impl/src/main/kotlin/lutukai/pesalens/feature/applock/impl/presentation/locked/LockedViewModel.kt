package lutukai.pesalens.feature.applock.impl.presentation.locked

import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lutukai.pesalens.core.domain.repository.AppLockRepository
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.model.PinCheck
import lutukai.pesalens.core.ui.mvi.MviViewModel
import lutukai.pesalens.feature.applock.impl.biometric.BiometricOutcome

private const val PinLength = 4
private const val MillisPerSecond = 1000L

class LockedViewModel(
    private val lock: AppLockRepository,
    private val clock: Clock,
) : MviViewModel<LockedUiState, LockedIntent, LockedEffect>(LockedUiState.Pin(digits = 0, error = null)) {

    private var loadJob: Job? = null

    // The typed digits stay here, never in state or logs.
    private var entry = ""
    private var cooldownEndsAt: Instant? = null
    private var verifying = false
    private var unlocked = false

    override fun handleIntent(intent: LockedIntent) = when (intent) {
        LockedIntent.Load -> load()
        is LockedIntent.Digit -> digit(intent.d)
        LockedIntent.Delete -> delete()
        LockedIntent.UnlockWithBiometric -> if (!unlocked) emit(LockedEffect.LaunchBiometricUnlock) else Unit
        is LockedIntent.BiometricResult -> biometricResult(intent.outcome)
    }

    /**
     * Reads the stored lock once. A stale cooldown (end time already past, attempts flag still set) is judged
     * against the clock, never by the flag alone.
     */
    private fun load() {
        if (loadJob != null) return
        loadJob = viewModelScope.launch {
            val stored = lock.state.first()
            when (stored.method) {
                LockMethod.Biometric -> {
                    reduce { LockedUiState.Biometric }
                    emit(LockedEffect.LaunchBiometricUnlock)
                }
                LockMethod.Pin -> {
                    cooldownEndsAt = stored.cooldownEndsAt
                    reduce { LockedUiState.Pin(digits = 0, error = coolingError()) }
                }
                LockMethod.None -> unlock()
            }
        }
    }

    /** The cooldown error with whole seconds remaining (rounded up), or null once the cooldown has passed. */
    private fun coolingError(): LockedError.CoolingDown? {
        val until = cooldownEndsAt ?: return null
        val remainingMs = (until - clock.now()).inWholeMilliseconds
        if (remainingMs <= 0) {
            cooldownEndsAt = null
            return null
        }
        return LockedError.CoolingDown(((remainingMs + MillisPerSecond - 1) / MillisPerSecond).toInt())
    }

    private fun digit(d: Char) {
        if (currentState !is LockedUiState.Pin || verifying || unlocked || !d.isDigit()) return
        val cooling = coolingError()
        if (cooling != null) {
            reduce { LockedUiState.Pin(digits = 0, error = cooling) }
            return
        }
        if (entry.length >= PinLength) return
        entry += d
        reduce { LockedUiState.Pin(entry.length, error = null) }
        if (entry.length == PinLength) verify()
    }

    private fun verify() {
        val pin = entry
        entry = ""
        verifying = true
        viewModelScope.launch {
            try {
                when (val check = lock.verifyPin(pin)) {
                    PinCheck.Correct -> unlock()
                    is PinCheck.Wrong -> reduce { LockedUiState.Pin(digits = 0, error = LockedError.Incorrect) }
                    is PinCheck.CoolingDown -> {
                        cooldownEndsAt = check.until
                        reduce { LockedUiState.Pin(digits = 0, error = coolingError()) }
                    }
                }
            } finally {
                verifying = false
            }
        }
    }

    private fun delete() {
        if (currentState !is LockedUiState.Pin || verifying || unlocked) return
        val cooling = coolingError()
        if (cooling != null) {
            reduce { LockedUiState.Pin(digits = 0, error = cooling) }
            return
        }
        if (entry.isEmpty()) return
        entry = entry.dropLast(1)
        reduce { LockedUiState.Pin(entry.length, error = null) }
    }

    private fun biometricResult(outcome: BiometricOutcome) {
        when (outcome) {
            BiometricOutcome.Succeeded -> unlock()
            BiometricOutcome.Cancelled,
            BiometricOutcome.UsePinInstead,
            BiometricOutcome.NotAvailable,
            BiometricOutcome.Error,
            -> Unit
        }
    }

    /** Emits [LockedEffect.Unlocked] once, however often success is reported. */
    private fun unlock() {
        if (unlocked) return
        unlocked = true
        emit(LockedEffect.Unlocked)
    }
}
