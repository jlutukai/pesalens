plugins {
    id("pesalens.jvm.library")
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    api(libs.kotlinx.coroutines.core)
}
