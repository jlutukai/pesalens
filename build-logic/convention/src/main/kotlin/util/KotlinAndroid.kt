package util

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** Bytecode level for every module, Android and pure Kotlin alike. */
private val JAVA_VERSION = JavaVersion.VERSION_17
private val JVM_TARGET = JvmTarget.JVM_17

/**
 * Robolectric reaches into JDK internals (e.g. FileDescriptor on SDK 37), which JDK 17+ blocks
 * unless opened: https://robolectric.org/getting-started/#running-with-java-17-and-higher.
 * `--enable-native-access` silences the JDK 24+ warning when it loads its native runtime.
 */
private val ROBOLECTRIC_JVM_ARGS = listOf(
    "--enable-native-access=ALL-UNNAMED",
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--add-opens=java.base/java.util=ALL-UNNAMED",
    "--add-opens=java.base/java.io=ALL-UNNAMED",
    "--add-opens=java.base/java.net=ALL-UNNAMED",
    "--add-opens=java.base/java.security=ALL-UNNAMED",
    "--add-opens=java.base/java.text=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
    "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
)

/** Shared by the application and library conventions. Kotlin is AGP 9's built-in Kotlin. */
internal fun Project.configureKotlinAndroid(android: CommonExtension) {
    android.apply {
        compileSdk = libs.version("androidCompileSdk").toInt()
        namespace = pesalensNamespace()
        defaultConfig.minSdk = libs.version("androidMinSdk").toInt()

        compileOptions.sourceCompatibility = JAVA_VERSION
        compileOptions.targetCompatibility = JAVA_VERSION

        testOptions.unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty("robolectric.graphicsMode", "NATIVE")
                it.jvmArgs(ROBOLECTRIC_JVM_ARGS)
            }
        }
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions.jvmTarget.set(JVM_TARGET)
    }
}

/** Pure-Kotlin modules: Java and Kotlin must agree on the target, or KGP fails the build. */
internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }

    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions.jvmTarget.set(JVM_TARGET)
    }
}
