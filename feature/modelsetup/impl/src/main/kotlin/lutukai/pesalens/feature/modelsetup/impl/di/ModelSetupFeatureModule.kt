package lutukai.pesalens.feature.modelsetup.impl.di

import lutukai.pesalens.core.navigation.PesaPanes
import lutukai.pesalens.feature.modelsetup.api.ModelSetup
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupScreen
import lutukai.pesalens.feature.modelsetup.impl.presentation.ModelSetupViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel as koinViewModel
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val modelSetupFeatureModule = module {
    koinViewModel { params -> ModelSetupViewModel(params.get(), get(), get(), get()) }

    // An extra pane: beside Settings (or the chat) on wide windows. From Welcome there is no list pane below it, so
    // PesaListDetailSceneStrategy shows it as a single, full-window pane.
    navigation<ModelSetup>(metadata = PesaPanes.extra()) { ModelSetupScreen(it.origin) }
}
