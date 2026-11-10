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

    // Espresso 3.5.x (pulled by compose ui-test) breaks on SDK 37 under Robolectric; 3.7.0 fixes it.
    testImplementation(libs.androidx.test.espresso.core)
}
