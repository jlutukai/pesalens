plugins {
    id("pesalens.android.library")
    id("pesalens.koin")
}

dependencies {
    implementation(projects.core.domain)
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
