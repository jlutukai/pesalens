import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import util.configureKotlinJvm
import util.libs
import util.library
import util.pluginId

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("kotlin-jvm"))
        configureKotlinJvm()

        dependencies {
            "testImplementation"(libs.library("kotlin-test"))
        }
    }
}
