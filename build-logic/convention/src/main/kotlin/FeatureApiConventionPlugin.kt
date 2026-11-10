import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import util.libs
import util.library
import util.pluginId

class FeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("pesalens.android.library")
        pluginManager.apply(libs.pluginId("kotlin-serialization"))

        dependencies {
            "api"(project(":core:navigation"))
            "implementation"(libs.library("kotlinx-serialization-json"))
        }
    }
}
