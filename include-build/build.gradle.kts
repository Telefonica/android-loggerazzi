plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.publish)
}

allprojects {
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    detekt {
        source.from(files(projectDir))
        config.from(files("${rootProject.projectDir}/../build-tools/detekt/detekt.yml"))
        buildUponDefaultConfig = true
    }
}