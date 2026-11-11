plugins {
    id("pesalens.feature.impl")
}

dependencies {
    implementation(projects.feature.assistant.api)
    implementation(libs.androidx.biometric)
}
