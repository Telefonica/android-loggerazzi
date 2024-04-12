pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "loggerazzi"
include(":app")

includeBuild("include-build") {
    dependencySubstitution {
        //substitute(module("com.telefonica.loggerazzi:gradle-plugin")).using(project(":gradle-plugin"))
    }
}
include(":loggerazzi")
