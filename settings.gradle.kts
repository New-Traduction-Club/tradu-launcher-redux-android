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

// renpyandroid is kept as reference only, not compiled
include(":app", ":core", ":runtime-renpy784")

rootProject.name = "tradu-launcher-redux"
