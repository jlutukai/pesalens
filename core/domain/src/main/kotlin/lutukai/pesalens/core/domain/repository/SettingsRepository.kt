package lutukai.pesalens.core.domain.repository

import kotlinx.coroutines.flow.Flow
import lutukai.pesalens.core.model.AppSettings
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.ThemePreference

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setTheme(t: ThemePreference)

    suspend fun setAssistantMode(m: AssistantMode)

    suspend fun giveCloudConsent()

    suspend fun completeOnboarding()
}
