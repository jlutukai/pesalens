package lutukai.pesalens.feature.applock.impl.presentation.setup

import androidx.compose.runtime.Immutable
import lutukai.pesalens.core.model.LockMethod
import lutukai.pesalens.core.ui.mvi.UiEffect
import lutukai.pesalens.core.ui.mvi.UiIntent
import lutukai.pesalens.core.ui.mvi.UiState
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.applock.impl.biometric.BiometricOutcome

enum class PinStep { Create, Confirm }

enum class PinError { Weak, Mismatch, Incorrect }

/** The entered PIN is never part of state: only how many digits have been typed. */
@Immutable
sealed interface AppLockSetupUiState : UiState {
    data class Choose(val currentMethod: LockMethod, val origin: LockSetupOrigin) : AppLockSetupUiState

    data class EnterPin(val step: PinStep, val digits: Int, val error: PinError?) : AppLockSetupUiState
}

sealed interface AppLockSetupIntent : UiIntent {
    data object Load : AppLockSetupIntent
    data object ChooseBiometric : AppLockSetupIntent
    data object ChoosePin : AppLockSetupIntent
    data class Digit(val d: Char) : AppLockSetupIntent
    data object Delete : AppLockSetupIntent
    data object Back : AppLockSetupIntent
    data object Skip : AppLockSetupIntent
    data object TurnOff : AppLockSetupIntent
    data class BiometricResult(val outcome: BiometricOutcome) : AppLockSetupIntent
}

sealed interface AppLockSetupEffect : UiEffect {
    data object LaunchBiometricSetup : AppLockSetupEffect
    data object Finish : AppLockSetupEffect
    data object Close : AppLockSetupEffect
}
