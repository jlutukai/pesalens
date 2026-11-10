package lutukai.pesalens.feature.onboarding.impl

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import lutukai.pesalens.core.model.AppSettings
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.testing.MainDispatcherRule
import lutukai.pesalens.core.testing.fake.FakeSettingsRepository
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeEffect
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeIntent
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeViewModel
import org.junit.Rule
import org.junit.Test

class WelcomeViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun getStarted_withoutConsent_opensConsent() = runTest {
        val settings = FakeSettingsRepository(AppSettings(cloudConsentGiven = false))
        val vm = WelcomeViewModel(settings)
        vm.onIntent(WelcomeIntent.Load)
        vm.effects.test {
            vm.onIntent(WelcomeIntent.GetStarted)
            assertEquals(WelcomeEffect.OpenConsent, awaitItem())
        }
    }

    @Test
    fun getStarted_withConsent_setsCloudAndOpensLockSetup() = runTest {
        val settings = FakeSettingsRepository(AppSettings(assistantMode = AssistantMode.OnDevice, cloudConsentGiven = true))
        val vm = WelcomeViewModel(settings)
        vm.onIntent(WelcomeIntent.Load)
        assertEquals(true, vm.state.value.cloudConsentGiven)
        vm.effects.test {
            vm.onIntent(WelcomeIntent.GetStarted)
            assertEquals(WelcomeEffect.OpenAppLockSetup, awaitItem())
        }
        assertEquals(AssistantMode.Cloud, settings.settings.first().assistantMode)
    }

    @Test
    fun useOnDevice_opensModelSetup() = runTest {
        val vm = WelcomeViewModel(FakeSettingsRepository())
        vm.effects.test {
            vm.onIntent(WelcomeIntent.UseOnDeviceInstead)
            assertEquals(WelcomeEffect.OpenModelSetup, awaitItem())
        }
    }
}
