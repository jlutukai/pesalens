plugins {
    `kotlin-dsl`
}

dependencies {
    // Compile against the plugins whose types the conventions use. At runtime they come from
    // the root build.gradle.kts `apply false` block, which keeps every version in one place.
    compileOnly(libs.plugins.android.application.toDep())
    compileOnly(libs.plugins.android.library.toDep())
    compileOnly(libs.plugins.kotlin.jvm.toDep())
    compileOnly(libs.plugins.roborazzi.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "pesalens.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "pesalens.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "pesalens.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "pesalens.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("jvmLibrary") {
            id = "pesalens.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("koin") {
            id = "pesalens.koin"
            implementationClass = "KoinConventionPlugin"
        }
        register("featureApi") {
            id = "pesalens.feature.api"
            implementationClass = "FeatureApiConventionPlugin"
        }
        register("featureImpl") {
            id = "pesalens.feature.impl"
            implementationClass = "FeatureImplConventionPlugin"
        }
        register("roborazzi") {
            id = "pesalens.roborazzi"
            implementationClass = "RoborazziConventionPlugin"
        }
    }
}
