package lutukai.pesalens.app

import lutukai.pesalens.core.model.AppSettings
import lutukai.pesalens.core.navigation.PesaDestination
import lutukai.pesalens.feature.assistant.api.Assistant
import lutukai.pesalens.feature.onboarding.api.Welcome

/** Where a fresh back stack starts: onboarding until it has been completed, then the assistant. */
fun startDestination(settings: AppSettings): PesaDestination =
    if (settings.onboardingComplete) Assistant else Welcome
