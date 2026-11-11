package lutukai.pesalens.feature.settings.impl

import lutukai.pesalens.core.navigation.PesaDestination
import lutukai.pesalens.core.testing.fake.FakeNavigator
import lutukai.pesalens.feature.applock.api.AppLockSetup
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.modelsetup.api.ModelSetup
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin
import lutukai.pesalens.feature.onboarding.api.CloudConsent
import lutukai.pesalens.feature.onboarding.api.ConsentOrigin
import lutukai.pesalens.feature.settings.api.Settings
import lutukai.pesalens.feature.settings.impl.presentation.SettingsEffect
import lutukai.pesalens.feature.settings.impl.presentation.onSettingsEffect
import kotlin.test.Test
import kotlin.test.assertEquals

/** Stands in for the assistant, the list pane Settings opens beside. */
private data object Chat : PesaDestination

class SettingsNavigationTest {

    private val model = ModelSetup(ModelSetupOrigin.Settings)
    private val lock = AppLockSetup(LockSetupOrigin.Settings)

    private fun settingsOpen() = FakeNavigator(Chat).apply { navigate(Settings) }

    @Test
    fun close_removesSettings() {
        val navigator = settingsOpen()
        navigator.onSettingsEffect(SettingsEffect.Close)
        assertEquals(listOf<PesaDestination>(Chat), navigator.stack)
    }

    /** Side by side, Settings' back arrow used to pop the extra pane and then do nothing. */
    @Test
    fun close_withAnExtraPaneOpen_removesSettingsAndThePane() {
        val navigator = settingsOpen()
        navigator.onSettingsEffect(SettingsEffect.OpenModel)

        navigator.onSettingsEffect(SettingsEffect.Close)

        assertEquals(listOf<PesaDestination>(Chat), navigator.stack)
    }

    @Test
    fun close_twice_removesSettingsOnce() {
        val navigator = settingsOpen()
        navigator.onSettingsEffect(SettingsEffect.Close)
        navigator.onSettingsEffect(SettingsEffect.Close)
        assertEquals(listOf<PesaDestination>(Chat), navigator.stack)
    }

    @Test
    fun openModel_pushesBesideSettings() {
        val navigator = settingsOpen()
        navigator.onSettingsEffect(SettingsEffect.OpenModel)
        assertEquals(listOf(Chat, Settings, model), navigator.stack)
    }

    @Test
    fun openAppLock_whileModelSetupIsOpen_replacesIt() {
        val navigator = settingsOpen()
        navigator.onSettingsEffect(SettingsEffect.OpenModel)

        navigator.onSettingsEffect(SettingsEffect.OpenAppLock)

        assertEquals(listOf(Chat, Settings, lock), navigator.stack)
    }

    @Test
    fun openModel_whileAppLockIsOpen_replacesIt() {
        val navigator = settingsOpen()
        navigator.onSettingsEffect(SettingsEffect.OpenAppLock)

        navigator.onSettingsEffect(SettingsEffect.OpenModel)

        assertEquals(listOf(Chat, Settings, model), navigator.stack)
    }

    @Test
    fun openModel_whenAlreadyOpen_keepsOneCopy() {
        val navigator = settingsOpen()
        navigator.onSettingsEffect(SettingsEffect.OpenModel)
        navigator.onSettingsEffect(SettingsEffect.OpenModel)
        assertEquals(listOf(Chat, Settings, model), navigator.stack)
    }

    @Test
    fun openCloudConsent_pushesTheOverlay() {
        val navigator = settingsOpen()
        navigator.onSettingsEffect(SettingsEffect.OpenCloudConsent)
        assertEquals(listOf(Chat, Settings, CloudConsent(ConsentOrigin.Settings)), navigator.stack)
    }
}
