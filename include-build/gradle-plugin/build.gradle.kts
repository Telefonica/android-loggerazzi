plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-gradle-plugin")
    alias(libs.plugins.publish.plugin)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(gradleApi())
    implementation(libs.android.gradle)
    implementation(libs.android.builder.test.api)
    implementation(libs.android.ddmlib)
    implementation(libs.android.common)
}

gradlePlugin {
    plugins {
        create("loggerazzi-plugin") {
            id = "com.telefonica.loggerazzi-plugin"
            displayName = "Loggerazzi"
            description = "Logs snapshot testing for Android Instrumentation tests"
            implementationClass = "com.telefonica.loggerazzi.LoggerazziPlugin"
            website = "https://github.com/Telefonica/android-loggerazzi"
            vcsUrl = "https://github.com/Telefonica/android-loggerazzi"
            tags = listOf("android", "instrumentation", "testing", "logs")
        }
    }
}
