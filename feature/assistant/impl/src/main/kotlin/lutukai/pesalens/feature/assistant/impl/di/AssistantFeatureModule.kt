package lutukai.pesalens.feature.assistant.impl.di

import lutukai.pesalens.core.navigation.PesaPanes
import lutukai.pesalens.feature.assistant.api.Assistant
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantScreen
import lutukai.pesalens.feature.assistant.impl.presentation.AssistantViewModel
import lutukai.pesalens.feature.assistant.impl.presentation.DetailPlaceholder
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val assistantFeatureModule = module {
    viewModel { AssistantViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }

    navigation<Assistant>(metadata = PesaPanes.list { DetailPlaceholder() }) { AssistantScreen() }
}
