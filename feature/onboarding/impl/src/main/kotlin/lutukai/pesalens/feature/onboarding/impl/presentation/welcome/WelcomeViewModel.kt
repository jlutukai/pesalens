package lutukai.pesalens.feature.onboarding.impl.presentation.welcome

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import lutukai.pesalens.core.domain.repository.SettingsRepository
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.ui.mvi.MviViewModel

class WelcomeViewModel(
    private val settings: SettingsRepository,
) : MviViewModel<WelcomeUiState, WelcomeIntent, WelcomeEffect>(WelcomeUiState()) {

    override fun handleIntent(intent: WelcomeIntent) = when (intent) {
        WelcomeIntent.Load -> load()
        WelcomeIntent.GetStarted -> getStarted()
        WelcomeIntent.UseOnDeviceInstead -> emit(WelcomeEffect.OpenModelSetup)
    }

    private fun load() {
        settings.settings
            .map { it.cloudConsentGiven }
            .onEach { given -> reduce { copy(cloudConsentGiven = given) } }
            .launchIn(viewModelScope)
    }

    private fun getStarted() {
        if (!currentState.cloudConsentGiven) {
            emit(WelcomeEffect.OpenConsent)
            return
        }
        viewModelScope.launch {
            settings.setAssistantMode(AssistantMode.Cloud)
            emit(WelcomeEffect.OpenAppLockSetup)
        }
    }
}
