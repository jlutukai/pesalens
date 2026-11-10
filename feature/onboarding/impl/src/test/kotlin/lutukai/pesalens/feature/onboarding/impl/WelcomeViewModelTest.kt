package lutukai.pesalens.feature.onboarding.impl

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import lutukai.pesalens.core.domain.repository.SettingsRepository
import kotlinx.coroutines.test.runTest
import lutukai.pesalens.core.model.AppSettings
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.testing.MainDispatcherRule
import lutukai.pesalens.core.testing.fake.FakeSettingsRepository
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeEffect
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeIntent
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeViewModel
import lutukai.pesalens.core.testing.fake.FakeNavigator
import lutukai.pesalens.feature.modelsetup.api.ModelSetup
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin
import lutukai.pesalens.feature.onboarding.api.CloudConsent
import lutukai.pesalens.feature.onboarding.api.ConsentOrigin
import lutukai.pesalens.feature.onboarding.api.Welcome
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

    @Test
    fun getStarted_beforeLoad_withConsent_opensLockSetup() = runTest {
        val vm = WelcomeViewModel(FakeSettingsRepository(AppSettings(cloudConsentGiven = true)))
        vm.effects.test {
            vm.onIntent(WelcomeIntent.GetStarted)
            assertEquals(WelcomeEffect.OpenAppLockSetup, awaitItem())
        }
    }

    @Test
    fun load_twice_collectsOnce() = runTest {
        val counting = CountingSettings(FakeSettingsRepository())
        val vm = WelcomeViewModel(counting)
        vm.onIntent(WelcomeIntent.Load)
        vm.onIntent(WelcomeIntent.Load)
        assertEquals(1, counting.subscriptions)
    }

    @Test
    fun getStarted_doubleTap_withConsent_emitsSingleEffect() = runTest {
        val vm = WelcomeViewModel(FakeSettingsRepository(AppSettings(cloudConsentGiven = true)))
        vm.effects.test {
            vm.onIntent(WelcomeIntent.GetStarted)
            vm.onIntent(WelcomeIntent.GetStarted)
            assertEquals(WelcomeEffect.OpenAppLockSetup, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun useOnDevice_doubleTap_leavesSingleModelSetupOnStack() = runTest {
        val vm = WelcomeViewModel(FakeSettingsRepository())
        val navigator = FakeNavigator(Welcome)
        vm.effects.test {
            vm.onIntent(WelcomeIntent.UseOnDeviceInstead)
            vm.onIntent(WelcomeIntent.UseOnDeviceInstead)
            repeat(2) { if (awaitItem() == WelcomeEffect.OpenModelSetup) navigator.navigate(ModelSetup(ModelSetupOrigin.Welcome)) }
        }
        assertEquals(listOf(Welcome, ModelSetup(ModelSetupOrigin.Welcome)), navigator.stack)
    }

    @Test
    fun getStarted_withoutConsent_repeatedTap_leavesSingleConsentOnStack() = runTest {
        val vm = WelcomeViewModel(FakeSettingsRepository(AppSettings(cloudConsentGiven = false)))
        val navigator = FakeNavigator(Welcome)
        vm.effects.test {
            repeat(2) {
                vm.onIntent(WelcomeIntent.GetStarted)
                if (awaitItem() == WelcomeEffect.OpenConsent) navigator.navigate(CloudConsent(ConsentOrigin.Welcome))
            }
        }
        assertEquals(listOf(Welcome, CloudConsent(ConsentOrigin.Welcome)), navigator.stack)
    }

    private class CountingSettings(private val delegate: FakeSettingsRepository) : SettingsRepository by delegate {
        var subscriptions = 0
            private set
        override val settings: Flow<AppSettings> = delegate.settings.onStart { subscriptions++ }
    }
}
