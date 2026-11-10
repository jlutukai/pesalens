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

    // Only the Settings origin opens beside Settings; Welcome and Assistant fall back to a single pane.
    navigation<ModelSetup>(metadata = PesaPanes.extra()) { ModelSetupScreen(it.origin) }
}
