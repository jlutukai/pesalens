import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import util.libs
import util.library
import util.pluginId

class RoborazziConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("roborazzi"))

        extensions.configure<RoborazziExtension> {
            outputDir.set(file("src/test/screenshots"))
        }

        dependencies {
            "testImplementation"(libs.library("roborazzi"))
            "testImplementation"(libs.library("roborazzi-compose"))
            "testImplementation"(libs.library("roborazzi-junit-rule"))
            // Espresso 3.5.x (pulled by compose ui-test) breaks on SDK 37 under Robolectric; 3.7.0 fixes it.
            "testImplementation"(libs.library("androidx-test-espresso-core"))
        }
    }
}
