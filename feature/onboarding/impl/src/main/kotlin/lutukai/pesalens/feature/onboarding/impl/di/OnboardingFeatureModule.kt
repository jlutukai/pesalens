package lutukai.pesalens.feature.onboarding.impl.di

import lutukai.pesalens.core.navigation.PesaPanes
import lutukai.pesalens.feature.onboarding.api.CloudConsent
import lutukai.pesalens.feature.onboarding.api.Welcome
import lutukai.pesalens.feature.onboarding.impl.presentation.consent.CloudConsentSheet
import lutukai.pesalens.feature.onboarding.impl.presentation.consent.CloudConsentViewModel
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeScreen
import lutukai.pesalens.feature.onboarding.impl.presentation.welcome.WelcomeViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.koin.core.module.dsl.viewModel as koinViewModel

@OptIn(KoinExperimentalAPI::class)
val onboardingFeatureModule = module {
    viewModelOf(::WelcomeViewModel)
    koinViewModel { params -> CloudConsentViewModel(params.get(), get()) }

    navigation<Welcome> { WelcomeScreen() }
    navigation<CloudConsent>(metadata = PesaPanes.overlay()) { CloudConsentSheet(it.origin) }
}
