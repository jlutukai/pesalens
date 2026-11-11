plugins {
    id("pesalens.feature.impl")
}

dependencies {
    implementation(projects.feature.modelsetup.api)
    implementation(projects.feature.applock.api)
    implementation(projects.feature.onboarding.api)
}
