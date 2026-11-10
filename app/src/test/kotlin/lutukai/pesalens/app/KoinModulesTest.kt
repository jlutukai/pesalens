package lutukai.pesalens.app

import androidx.navigation3.runtime.entryProvider
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import lutukai.pesalens.app.di.appModules
import lutukai.pesalens.core.navigation.PesaDestination
import lutukai.pesalens.core.navigation.PesaPanes
import lutukai.pesalens.core.ui.message.UserMessageQueue
import lutukai.pesalens.core.ui.message.UserMessageSink
import lutukai.pesalens.feature.applock.api.AppLockSetup
import lutukai.pesalens.feature.applock.api.LockSetupOrigin
import lutukai.pesalens.feature.applock.api.Locked
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedViewModel
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupViewModel
import lutukai.pesalens.feature.assistant.api.Assistant
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantViewModel
import lutukai.pesalens.feature.modelsetup.api.ModelSetup
import lutukai.pesalens.feature.modelsetup.api.ModelSetupOrigin
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupViewModel
import lutukai.pesalens.feature.onboarding.api.CloudConsent
import lutukai.pesalens.feature.onboarding.api.ConsentOrigin
import lutukai.pesalens.feature.onboarding.api.Welcome
import lutukai.pesalens.feature.onboarding.impl.presentation.consent.CloudConsentViewModel
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeViewModel
import lutukai.pesalens.feature.settings.api.Settings
import lutukai.pesalens.feature.settings.impl.presentation.SettingsViewModel
import lutukai.pesalens.feature.transactions.api.CategoryTransactions
import lutukai.pesalens.feature.transactions.impl.presentation.TransactionsViewModel
import org.koin.compose.navigation3.EntryProviderInstaller
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class KoinModulesTest {

    private val app = koinApplication { modules(appModules) }
    private val koin = app.koin

    @AfterTest
    fun tearDown() = app.close()

    /** Static check: every constructor dependency of every definition is bound, apart from the destination origins. */
    @Test
    fun appModules_verify() {
        module { includes(appModules) }.verify(
            injections = injectedParameters(
                definition<CloudConsentViewModel>(ConsentOrigin::class),
                definition<AppLockSetupViewModel>(LockSetupOrigin::class),
                definition<ModelSetupViewModel>(ModelSetupOrigin::class),
            ),
        )
    }

    @Test
    fun everyViewModel_resolves() {
        koin.get<WelcomeViewModel>()
        koin.get<CloudConsentViewModel> { parametersOf(ConsentOrigin.Welcome) }
        koin.get<AppLockSetupViewModel> { parametersOf(LockSetupOrigin.Onboarding) }
        koin.get<LockedViewModel>()
        koin.get<AssistantViewModel>()
        koin.get<TransactionsViewModel>()
        koin.get<SettingsViewModel>()
        koin.get<ModelSetupViewModel> { parametersOf(ModelSetupOrigin.Settings) }
    }

    @Test
    fun userMessageSink_isTheQueueTheShellCollects() {
        assertTrue(koin.get<UserMessageSink>() === koin.get<UserMessageQueue>())
    }

    @Test
    fun everyDestination_hasAnEntry() {
        val installers = koin.getAll<EntryProviderInstaller>()
        val provide = entryProvider<Any> { installers.forEach { install -> install(this) } }
        val destinations: List<PesaDestination> = listOf(
            Welcome,
            CloudConsent(ConsentOrigin.Welcome),
            AppLockSetup(LockSetupOrigin.Onboarding),
            Locked,
            Assistant,
            CategoryTransactions("food", 2026, 11),
            Settings,
            ModelSetup(ModelSetupOrigin.Settings),
        )

        // The entry provider throws for a key no installer handles.
        val entries = destinations.associateWith { provide(it) }

        assertEquals(destinations.size, installers.size)
        assertTrue(PesaPanes.OverlayKey in entries.getValue(CloudConsent(ConsentOrigin.Welcome)).metadata)
    }
}
