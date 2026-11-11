package lutukai.pesalens.feature.applock.impl.di

import lutukai.pesalens.core.navigation.PesaPanes
import lutukai.pesalens.feature.applock.api.AppLockSetup
import lutukai.pesalens.feature.applock.api.Locked
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedScreen
import lutukai.pesalens.feature.applock.impl.presentation.locked.LockedViewModel
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupScreen
import lutukai.pesalens.feature.applock.impl.presentation.setup.AppLockSetupViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel as koinViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val appLockFeatureModule = module {
    koinViewModel { params -> AppLockSetupViewModel(params.get(), get(), get(), get()) }
    viewModelOf(::LockedViewModel)

    navigation<AppLockSetup>(metadata = PesaPanes.extra()) { AppLockSetupScreen(it) }
    navigation<Locked> { LockedScreen() }
}
