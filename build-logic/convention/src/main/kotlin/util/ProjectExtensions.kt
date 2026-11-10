package util

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.version(alias: String): String =
    findVersion(alias).orElseThrow { missing("version", alias) }.requiredVersion

fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { missing("library", alias) }

fun VersionCatalog.pluginId(alias: String): String =
    findPlugin(alias).orElseThrow { missing("plugin", alias) }.get().pluginId

private fun missing(kind: String, alias: String) =
    IllegalArgumentException("No $kind '$alias' in gradle/libs.versions.toml")

/**
 * Derives the Android namespace from the Gradle project path.
 * e.g. ":core:designsystem" -> "lutukai.pesalens.core.designsystem"
 *      ":feature:assistant:impl" -> "lutukai.pesalens.feature.assistant.impl"
 */
fun Project.pesalensNamespace(): String =
    path.split(":").filter { it.isNotEmpty() }.joinToString(separator = ".", prefix = "lutukai.pesalens.")
