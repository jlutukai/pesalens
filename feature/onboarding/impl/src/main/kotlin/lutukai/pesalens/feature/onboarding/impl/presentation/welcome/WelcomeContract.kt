package lutukai.pesalens.feature.onboarding.impl.presentation.welcome

import androidx.compose.runtime.Immutable
import lutukai.pesalens.core.ui.mvi.UiEffect
import lutukai.pesalens.core.ui.mvi.UiIntent
import lutukai.pesalens.core.ui.mvi.UiState

@Immutable
data class WelcomeUiState(val cloudConsentGiven: Boolean = false) : UiState

sealed interface WelcomeIntent : UiIntent {
    data object Load : WelcomeIntent
    data object GetStarted : WelcomeIntent
    data object UseOnDeviceInstead : WelcomeIntent
}

sealed interface WelcomeEffect : UiEffect {
    data object OpenConsent : WelcomeEffect
    data object OpenAppLockSetup : WelcomeEffect
    data object OpenModelSetup : WelcomeEffect
}
