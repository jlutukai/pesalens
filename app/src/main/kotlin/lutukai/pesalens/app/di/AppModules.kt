package lutukai.pesalens.app.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import lutukai.pesalens.app.AppLockSession
import lutukai.pesalens.core.data.di.dataModule
import lutukai.pesalens.core.ui.message.UserMessageQueue
import lutukai.pesalens.core.ui.message.UserMessageSink
import lutukai.pesalens.feature.applock.impl.di.appLockFeatureModule
import lutukai.pesalens.feature.assistant.impl.di.assistantFeatureModule
import lutukai.pesalens.feature.modelsetup.impl.di.modelSetupFeatureModule
import lutukai.pesalens.feature.onboarding.impl.di.onboardingFeatureModule
import lutukai.pesalens.feature.settings.impl.di.settingsFeatureModule
import lutukai.pesalens.feature.transactions.impl.di.transactionsFeatureModule
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * The shell's process-wide state: the one queue features post messages to (as [UserMessageSink]) and the snackbar host
 * collects, and the app lock session, which records on the main thread when a lock is owed.
 */
private val shellModule = module {
    single { UserMessageQueue() } bind UserMessageSink::class
    single { AppLockSession(get(), CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)) }
}

/** Every Koin module the app runs with: data bindings, the shell's state, and each feature's ViewModels and entries. */
val appModules: List<Module> = listOf(
    dataModule,
    shellModule,
    onboardingFeatureModule,
    appLockFeatureModule,
    assistantFeatureModule,
    transactionsFeatureModule,
    settingsFeatureModule,
    modelSetupFeatureModule,
)
