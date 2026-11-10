plugins {
    id("pesalens.android.application.compose")
    id("pesalens.koin")
    id("pesalens.roborazzi")
}

android {
    defaultConfig {
        applicationId = "lutukai.pesalens.app"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
}

dependencies {
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.onboarding.impl)
    implementation(projects.feature.applock.api)
    implementation(projects.feature.applock.impl)
    implementation(projects.feature.assistant.api)
    implementation(projects.feature.assistant.impl)
    implementation(projects.feature.transactions.api)
    implementation(projects.feature.transactions.impl)
    implementation(projects.feature.settings.api)
    implementation(projects.feature.settings.impl)
    implementation(projects.feature.modelsetup.api)
    implementation(projects.feature.modelsetup.impl)

    implementation(projects.core.data)
    implementation(projects.core.navigation)
    implementation(projects.core.ui)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.biometric)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.navigation3)

    // junit, kotlin-test-junit, robolectric and ui-test-junit4 come from pesalens.android.application.compose;
    // the roborazzi libraries from pesalens.roborazzi.
    testImplementation(projects.core.testing)
    testImplementation(libs.koin.test)
}
