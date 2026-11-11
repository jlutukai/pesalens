package lutukai.pesalens.feature.onboarding.impl.presentation.consent

import lutukai.pesalens.core.ui.mvi.UiEffect
import lutukai.pesalens.core.ui.mvi.UiIntent
import lutukai.pesalens.core.ui.mvi.UiState

/** The consent sheet has no data to show, so its state is a single value. */
data object CloudConsentUiState : UiState

sealed interface CloudConsentIntent : UiIntent {
    data object Agree : CloudConsentIntent
    data object NotNow : CloudConsentIntent
}

sealed interface CloudConsentEffect : UiEffect {
    data object Close : CloudConsentEffect
    data object ContinueOnboarding : CloudConsentEffect
}
