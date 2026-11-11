plugins {
    id("pesalens.android.library.compose")
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    api(projects.core.domain)
    api(projects.core.designsystem)
    api(projects.core.ui)
    api(projects.core.navigation)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(libs.junit)
    api(libs.androidx.compose.ui.test.junit4)
    api(libs.roborazzi.compose)
}
