plugins {
    id("pesalens.android.library.compose")
    id("pesalens.roborazzi")
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.compose)
}
