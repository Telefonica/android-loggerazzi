plugins {
    id("org.jetbrains.kotlin.jvm")
    id("java-gradle-plugin")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("com.android.tools.build:gradle:8.3.1")
    compileOnly("com.android.tools.build:builder-test-api:8.3.1")
    compileOnly("com.android.tools.ddms:ddmlib:31.2.2")
    compileOnly("com.android.tools:common:31.2.2")
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