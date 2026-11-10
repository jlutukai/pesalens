plugins {
    id("pesalens.feature.impl")
}

dependencies {
    implementation(projects.feature.modelsetup.api)
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.settings.api)
    implementation(projects.feature.transactions.api)
}
