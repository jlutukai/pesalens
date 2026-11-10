package lutukai.pesalens.core.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import lutukai.pesalens.core.model.AppSettings
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.model.ThemePreference
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionSettingsRepositoryTest {
    private val repo = SessionSettingsRepository()

    @Test
    fun defaults_equalAppSettings() = runTest {
        assertEquals(AppSettings(), repo.settings.first())
    }

    @Test
    fun setters_areReflectedInSettings() = runTest {
        repo.setTheme(ThemePreference.Dark)
        assertEquals(ThemePreference.Dark, repo.settings.first().theme)
        repo.setAssistantMode(AssistantMode.OnDevice)
        assertEquals(AssistantMode.OnDevice, repo.settings.first().assistantMode)
        repo.giveCloudConsent()
        assertEquals(true, repo.settings.first().cloudConsentGiven)
        repo.completeOnboarding()
        assertEquals(true, repo.settings.first().onboardingComplete)
    }
}
