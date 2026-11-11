package lutukai.pesalens.feature.modelsetup.impl

import lutukai.pesalens.core.navigation.PesaDestination
import lutukai.pesalens.core.testing.fake.FakeNavigator
import lutukai.pesalens.feature.applock.api.AppLockSetup
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.modelsetup.api.ModelSetup
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupEffect
import lutukai.pesalens.feature.modelsetup.impl.presentation.consentOrigin
import lutukai.pesalens.feature.modelsetup.impl.presentation.onModelSetupEffect
import lutukai.pesalens.feature.onboarding.api.CloudConsent
import lutukai.pesalens.feature.onboarding.api.ConsentOrigin
import kotlin.test.Test
import kotlin.test.assertEquals

/** Stand-ins for the screens below model setup. */
private data object Root : PesaDestination
private data object SettingsPane : PesaDestination

class ModelSetupNavigationTest {

    @Test
    fun consentOrigin_continuesOnboardingOnlyFromWelcome() {
        assertEquals(ConsentOrigin.ModelSetup, ModelSetupOrigin.Welcome.consentOrigin())
        assertEquals(ConsentOrigin.Settings, ModelSetupOrigin.Settings.consentOrigin())
        assertEquals(ConsentOrigin.Assistant, ModelSetupOrigin.Assistant.consentOrigin())
    }

    @Test
    fun openCloudConsent_fromWelcome_opensTheOnboardingSheet() {
        val navigator = FakeNavigator(Root).apply { navigate(ModelSetup(ModelSetupOrigin.Welcome)) }
        navigator.onModelSetupEffect(ModelSetupEffect.OpenCloudConsent, ModelSetupOrigin.Welcome)
        assertEquals(CloudConsent(ConsentOrigin.ModelSetup), navigator.current)
    }

    /** After onboarding, agreeing must not replace the back stack with onboarding's lock setup. */
    @Test
    fun openCloudConsent_fromSettings_opensASheetThatJustCloses() {
        val navigator = FakeNavigator(Root).apply { navigate(SettingsPane); navigate(ModelSetup(ModelSetupOrigin.Settings)) }
        navigator.onModelSetupEffect(ModelSetupEffect.OpenCloudConsent, ModelSetupOrigin.Settings)
        assertEquals(CloudConsent(ConsentOrigin.Settings), navigator.current)
    }

    @Test
    fun close_removesModelSetupItself() {
        val model = ModelSetup(ModelSetupOrigin.Settings)
        val navigator = FakeNavigator(Root).apply { navigate(SettingsPane); navigate(model) }

        navigator.onModelSetupEffect(ModelSetupEffect.Close, ModelSetupOrigin.Settings)

        assertEquals(listOf(Root, SettingsPane), navigator.stack)
    }

    @Test
    fun close_twice_removesOnce() {
        val navigator = FakeNavigator(Root).apply { navigate(SettingsPane); navigate(ModelSetup(ModelSetupOrigin.Settings)) }
        repeat(2) { navigator.onModelSetupEffect(ModelSetupEffect.Close, ModelSetupOrigin.Settings) }
        assertEquals(listOf(Root, SettingsPane), navigator.stack)
    }

    @Test
    fun continueOnboarding_replacesTheStackWithLockSetup() {
        val navigator = FakeNavigator(Root).apply { navigate(ModelSetup(ModelSetupOrigin.Welcome)) }
        navigator.onModelSetupEffect(ModelSetupEffect.ContinueOnboarding, ModelSetupOrigin.Welcome)
        assertEquals(listOf<PesaDestination>(AppLockSetup(LockSetupOrigin.Onboarding)), navigator.stack)
    }
}
