import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import util.libs
import util.library

class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        dependencies {
            "implementation"(platform(libs.library("koin-bom")))
            "implementation"(libs.library("koin-core"))
        }

        pluginManager.withPlugin("com.android.base") {
            dependencies {
                "implementation"(libs.library("koin-android"))
            }
        }
    }
}
