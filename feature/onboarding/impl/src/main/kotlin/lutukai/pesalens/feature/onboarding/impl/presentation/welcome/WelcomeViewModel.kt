package lutukai.pesalens.feature.onboarding.impl.presentation.welcome

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
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

    private var loadJob: Job? = null
    private var getStartedJob: Job? = null
    private var leftWelcome = false

    override fun handleIntent(intent: WelcomeIntent) = when (intent) {
        WelcomeIntent.Load -> load()
        WelcomeIntent.GetStarted -> getStarted()
        WelcomeIntent.UseOnDeviceInstead -> if (!leftWelcome) emit(WelcomeEffect.OpenModelSetup) else Unit
    }

    /** Collects settings once for the life of the ViewModel, however often the screen re-enters composition. */
    private fun load() {
        if (loadJob != null) return
        loadJob = settings.settings
            .map { it.cloudConsentGiven }
            .onEach { given -> reduce { copy(cloudConsentGiven = given) } }
            .launchIn(viewModelScope)
    }

    /** Reads consent from the repository, not state, so a tap before the first emission still branches correctly. */
    private fun getStarted() {
        if (leftWelcome || getStartedJob?.isActive == true) return
        getStartedJob = viewModelScope.launch {
            if (settings.settings.first().cloudConsentGiven) {
                settings.setAssistantMode(AssistantMode.Cloud)
                leftWelcome = true
                emit(WelcomeEffect.OpenAppLockSetup)
            } else {
                emit(WelcomeEffect.OpenConsent)
            }
        }
    }
}
