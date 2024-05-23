plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-gradle-plugin")
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
        create("loggerazzi") {
            id = "com.telefonica.loggerazzi"
            implementationClass = "com.telefonica.loggerazzi.LoggerazziPlugin"
        }
    }
}

allprojects {
    group = "com.telefonica.loggerazzi"
    version = System.getProperty("LIBRARY_VERSION") ?: "undefined"
}

apply("${rootProject.projectDir}/mavencentral.gradle")