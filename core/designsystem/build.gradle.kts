plugins {
    id("pesalens.android.library.compose")
    id("pesalens.roborazzi")
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.adaptive)
    api(libs.phosphor.icon)
    api(libs.kotlinx.collections.immutable)
}
