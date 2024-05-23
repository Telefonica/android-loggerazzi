// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.publish)
    alias(libs.plugins.detekt)
}

allprojects {
    group = "com.telefonica.loggerazzi"
    version = System.getProperty("LIBRARY_VERSION") ?: "undefined"

    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    detekt {
        source.from(files(projectDir))
        config.from(files("${rootProject.projectDir}/build-tools/detekt/detekt.yml"))
        buildUponDefaultConfig = true
    }
}

apply("${rootProject.projectDir}/publish_maven_central.gradle")