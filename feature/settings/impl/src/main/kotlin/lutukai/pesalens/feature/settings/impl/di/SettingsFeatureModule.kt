package lutukai.pesalens.feature.settings.impl.di

import lutukai.pesalens.core.navigation.PesaPanes
import lutukai.pesalens.feature.settings.api.Settings
import lutukai.pesalens.feature.settings.impl.presentation.SettingsScreen
import lutukai.pesalens.feature.settings.impl.presentation.SettingsViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val settingsFeatureModule = module {
    viewModelOf(::SettingsViewModel)

    navigation<Settings>(metadata = PesaPanes.detail()) { SettingsScreen() }
}
