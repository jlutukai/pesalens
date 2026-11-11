package lutukai.pesalens.feature.onboarding.impl

import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import lutukai.pesalens.core.model.AppSettings
import lutukai.pesalens.core.model.AssistantMode
import lutukai.pesalens.core.testing.MainDispatcherRule
import lutukai.pesalens.core.testing.fake.FakeSettingsRepository
import lutukai.pesalens.feature.onboarding.api.ConsentOrigin
import lutukai.pesalens.feature.onboarding.impl.presentation.consent.CloudConsentEffect
import lutukai.pesalens.feature.onboarding.impl.presentation.consent.CloudConsentIntent
import lutukai.pesalens.feature.onboarding.impl.presentation.consent.CloudConsentViewModel
import org.junit.Rule
import org.junit.Test

class CloudConsentViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun agree_fromWelcome_givesConsentSetsCloudAndContinues() = runTest {
        val settings = FakeSettingsRepository(AppSettings(assistantMode = AssistantMode.OnDevice))
        val vm = CloudConsentViewModel(ConsentOrigin.Welcome, settings)
        vm.effects.test {
            vm.onIntent(CloudConsentIntent.Agree)
            assertEquals(CloudConsentEffect.ContinueOnboarding, awaitItem())
        }
        val saved = settings.settings.first()
        assertTrue(saved.cloudConsentGiven)
        assertEquals(AssistantMode.Cloud, saved.assistantMode)
    }

    @Test
    fun agree_fromSettings_givesConsentSetsCloudAndCloses() = runTest {
        val settings = FakeSettingsRepository(AppSettings(assistantMode = AssistantMode.OnDevice))
        val vm = CloudConsentViewModel(ConsentOrigin.Settings, settings)
        vm.effects.test {
            vm.onIntent(CloudConsentIntent.Agree)
            assertEquals(CloudConsentEffect.Close, awaitItem())
        }
        val saved = settings.settings.first()
        assertTrue(saved.cloudConsentGiven)
        assertEquals(AssistantMode.Cloud, saved.assistantMode)
    }

    @Test
    fun notNow_closes() = runTest {
        val settings = FakeSettingsRepository()
        val vm = CloudConsentViewModel(ConsentOrigin.Welcome, settings)
        vm.effects.test {
            vm.onIntent(CloudConsentIntent.NotNow)
            assertEquals(CloudConsentEffect.Close, awaitItem())
        }
        assertEquals(false, settings.settings.first().cloudConsentGiven)
    }

    @Test
    fun agree_fromModelSetup_continuesOnboarding() = runTest {
        val vm = CloudConsentViewModel(ConsentOrigin.ModelSetup, FakeSettingsRepository())
        vm.effects.test {
            vm.onIntent(CloudConsentIntent.Agree)
            assertEquals(CloudConsentEffect.ContinueOnboarding, awaitItem())
        }
    }

    /** Model setup opened after onboarding passes Settings or Assistant, so agreeing must not restart onboarding. */
    @Test
    fun agree_fromAssistant_closes() = runTest {
        val vm = CloudConsentViewModel(ConsentOrigin.Assistant, FakeSettingsRepository())
        vm.effects.test {
            vm.onIntent(CloudConsentIntent.Agree)
            assertEquals(CloudConsentEffect.Close, awaitItem())
        }
    }

    @Test
    fun agree_doubleTap_emitsSingleEffect() = runTest {
        val vm = CloudConsentViewModel(ConsentOrigin.Settings, FakeSettingsRepository())
        vm.effects.test {
            vm.onIntent(CloudConsentIntent.Agree)
            vm.onIntent(CloudConsentIntent.Agree)
            vm.onIntent(CloudConsentIntent.NotNow)
            assertEquals(CloudConsentEffect.Close, awaitItem())
            expectNoEvents()
        }
    }
}
