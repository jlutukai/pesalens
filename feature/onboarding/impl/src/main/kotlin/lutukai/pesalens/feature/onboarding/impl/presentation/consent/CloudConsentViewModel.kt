package lutukai.pesalens.feature.onboarding.impl.presentation.consent

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lutukai.pesalens.core.domain.repository.SettingsRepository
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.ui.mvi.MviViewModel
import lutukai.pesalens.feature.onboarding.api.ConsentOrigin

class CloudConsentViewModel(
    private val origin: ConsentOrigin,
    private val settings: SettingsRepository,
) : MviViewModel<CloudConsentUiState, CloudConsentIntent, CloudConsentEffect>(CloudConsentUiState) {

    private var finished = false

    override fun handleIntent(intent: CloudConsentIntent) = when (intent) {
        CloudConsentIntent.Agree -> agree()
        CloudConsentIntent.NotNow -> finish(CloudConsentEffect.Close)
    }

    /** The sheet is dismissed after one navigation effect; further taps (double tap) are ignored. */
    private fun finish(effect: CloudConsentEffect) {
        if (finished) return
        finished = true
        emit(effect)
    }

    private fun agree() {
        if (finished) return
        finished = true
        viewModelScope.launch {
            settings.giveCloudConsent()
            settings.setAssistantMode(AssistantMode.Cloud)
            emit(
                when (origin) {
                    ConsentOrigin.Welcome, ConsentOrigin.ModelSetup -> CloudConsentEffect.ContinueOnboarding
                    ConsentOrigin.Settings, ConsentOrigin.Assistant -> CloudConsentEffect.Close
                },
            )
        }
    }
}
