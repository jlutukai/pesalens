package lutukai.pesalens.app.di

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

/** The one queue features post messages to (as [UserMessageSink]) and the shell's snackbar host collects. */
private val userMessageModule = module {
    single { UserMessageQueue() } bind UserMessageSink::class
}

/** Every Koin module the app runs with: data bindings, the message queue, and each feature's ViewModels and entries. */
val appModules: List<Module> = listOf(
    dataModule,
    userMessageModule,
    onboardingFeatureModule,
    appLockFeatureModule,
    assistantFeatureModule,
    transactionsFeatureModule,
    settingsFeatureModule,
    modelSetupFeatureModule,
)
