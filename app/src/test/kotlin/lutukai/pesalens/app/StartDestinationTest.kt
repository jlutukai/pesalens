package lutukai.pesalens.app

import kotlin.test.Test
import kotlin.test.assertEquals
import lutukai.pesalens.core.model.AppSettings
import lutukai.pesalens.feature.assistant.api.Assistant
import lutukai.pesalens.feature.onboarding.api.Welcome

class StartDestinationTest {

    @Test
    fun defaultSettings_startAtWelcome() {
        assertEquals(Welcome, startDestination(AppSettings()))
    }

    @Test
    fun onboardingComplete_startsAtAssistant() {
        assertEquals(Assistant, startDestination(AppSettings(onboardingComplete = true)))
    }
}
