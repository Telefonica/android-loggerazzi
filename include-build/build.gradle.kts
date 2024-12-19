plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.detekt)
}

allprojects {
    group = "com.telefonica.loggerazzi"
    version = System.getProperty("LIBRARY_VERSION") ?: "undefined"

    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)
    detekt {
        source.from(files(projectDir))
        config.from(files("${rootProject.projectDir}/../build-tools/detekt/detekt.yml"))
        buildUponDefaultConfig = true
    }
}
