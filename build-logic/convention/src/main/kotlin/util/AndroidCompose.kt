package util

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/** Shared by the application and library compose conventions. */
internal fun Project.configureAndroidCompose(android: CommonExtension) {
    pluginManager.apply(libs.pluginId("kotlin-compose"))

    android.buildFeatures.compose = true

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions.optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
        )
    }

    dependencies {
        "implementation"(platform(libs.library("androidx-compose-bom")))
        "implementation"(libs.library("androidx-compose-ui"))
        "implementation"(libs.library("androidx-compose-ui-tooling-preview"))
        "implementation"(libs.library("androidx-compose-foundation"))
        "implementation"(libs.library("androidx-compose-material3"))
        "implementation"(libs.library("kotlinx-collections-immutable"))

        "debugImplementation"(libs.library("androidx-compose-ui-tooling"))
        "debugImplementation"(libs.library("androidx-compose-ui-test-manifest"))

        "testImplementation"(libs.library("junit"))
        // AGP's built-in Kotlin, unlike KGP's JVM plugin, does not pick kotlin-test's JUnit 4
        // variant from the test task, so kotlin.test.Test would not resolve without this.
        "testImplementation"(libs.library("kotlin-test-junit"))
        "testImplementation"(libs.library("turbine"))
        "testImplementation"(libs.library("kotlinx-coroutines-test"))
        "testImplementation"(libs.library("robolectric"))
        "testImplementation"(libs.library("androidx-compose-ui-test-junit4"))
    }
}
