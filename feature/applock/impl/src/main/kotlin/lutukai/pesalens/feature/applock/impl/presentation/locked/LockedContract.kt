package lutukai.pesalens.feature.applock.impl.presentation.locked

import androidx.compose.runtime.Immutable
import lutukai.pesalens.core.ui.mvi.UiEffect
import lutukai.pesalens.core.ui.mvi.UiIntent
import lutukai.pesalens.core.ui.mvi.UiState
import lutukai.pesalens.feature.applock.impl.biometric.BiometricOutcome

/** The entered PIN is never part of state: only how many digits have been typed. */
@Immutable
sealed interface LockedUiState : UiState {
    data class Pin(val digits: Int, val error: LockedError?) : LockedUiState

    data object Biometric : LockedUiState
}

@Immutable
sealed interface LockedError {
    data object Incorrect : LockedError

    data class CoolingDown(val seconds: Int) : LockedError
}

sealed interface LockedIntent : UiIntent {
    data object Load : LockedIntent
    data class Digit(val d: Char) : LockedIntent
    data object Delete : LockedIntent
    data object UnlockWithBiometric : LockedIntent
    data class BiometricResult(val outcome: BiometricOutcome) : LockedIntent
}

sealed interface LockedEffect : UiEffect {
    data object LaunchBiometricUnlock : LockedEffect
    data object Unlocked : LockedEffect
}
