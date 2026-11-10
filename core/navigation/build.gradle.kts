plugins {
    id("pesalens.android.library.compose")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
    api(libs.androidx.compose.material3.adaptive.navigation3)
    api(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(projects.core.designsystem)
}
