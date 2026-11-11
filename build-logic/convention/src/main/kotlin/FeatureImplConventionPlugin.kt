import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import util.libs
import util.library

class FeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        require(path.endsWith(":impl")) { "pesalens.feature.impl applies only to :impl modules, not $path" }

        pluginManager.apply("pesalens.android.library.compose")
        pluginManager.apply("pesalens.koin")
        pluginManager.apply("pesalens.roborazzi")

        dependencies {
            "implementation"(project(path.replace(":impl", ":api")))
            for (core in listOf("model", "common", "domain", "designsystem", "ui", "navigation")) {
                "implementation"(project(":core:$core"))
            }

            "implementation"(libs.library("koin-compose"))
            "implementation"(libs.library("koin-compose-viewmodel"))
            "implementation"(libs.library("koin-compose-navigation3"))
            "implementation"(libs.library("androidx-lifecycle-runtime-compose"))
            "implementation"(libs.library("androidx-lifecycle-viewmodel-compose"))

            // junit, kotlin-test-junit, turbine, coroutines-test, robolectric and ui-test-junit4 come from
            // pesalens.android.library.compose; the roborazzi libraries from pesalens.roborazzi.
            "testImplementation"(project(":core:testing"))
        }
    }
}
