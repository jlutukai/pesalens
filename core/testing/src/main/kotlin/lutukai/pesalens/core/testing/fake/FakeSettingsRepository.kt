package lutukai.pesalens.core.testing.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import lutukai.pesalens.core.domain.repository.SettingsRepository
import lutukai.pesalens.core.model.AppSettings
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.ThemePreference

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    private val state = MutableStateFlow(initial)

    override val settings: Flow<AppSettings> = state

    override suspend fun setTheme(t: ThemePreference) = state.update { it.copy(theme = t) }

    override suspend fun setAssistantMode(m: AssistantMode) = state.update { it.copy(assistantMode = m) }

    override suspend fun giveCloudConsent() = state.update { it.copy(cloudConsentGiven = true) }

    override suspend fun completeOnboarding() = state.update { it.copy(onboardingComplete = true) }
}
