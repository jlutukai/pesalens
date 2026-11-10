package lutukai.pesalens.core.data.di

import kotlinx.datetime.TimeZone
import lutukai.pesalens.core.data.InMemoryConversationSession
import lutukai.pesalens.core.data.NotConnectedFinanceRepository
import lutukai.pesalens.core.data.NotConnectedIntentInterpreter
import lutukai.pesalens.core.data.NotConnectedModelManager
import lutukai.pesalens.core.data.SessionAppLockRepository
import lutukai.pesalens.core.data.SessionSettingsRepository
import lutukai.pesalens.core.domain.ai.IntentInterpreter
import lutukai.pesalens.core.domain.ai.ModelManager
import lutukai.pesalens.core.domain.repository.AppLockRepository
import lutukai.pesalens.core.domain.repository.ConversationSession
import lutukai.pesalens.core.domain.repository.FinanceRepository
import lutukai.pesalens.core.domain.repository.SettingsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

/** Phase-1 bindings: session state where it is genuine, "not connected" everywhere else. */
val dataModule = module {
    single<Clock> { Clock.System }
    single { TimeZone.currentSystemDefault() }
    singleOf(::SessionSettingsRepository) bind SettingsRepository::class
    singleOf(::SessionAppLockRepository) bind AppLockRepository::class
    singleOf(::InMemoryConversationSession) bind ConversationSession::class
    singleOf(::NotConnectedFinanceRepository) bind FinanceRepository::class
    singleOf(::NotConnectedIntentInterpreter) bind IntentInterpreter::class
    singleOf(::NotConnectedModelManager) bind ModelManager::class
}
