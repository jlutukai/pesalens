plugins {
    id("pesalens.feature.impl")
}

dependencies {
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.applock.api)
}
