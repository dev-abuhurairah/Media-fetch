pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI.create("https://jitpack.io") }
    }
}

rootProject.name = "MediaFetch"

include(":app")
include(":core:model")
include(":core:common")
include(":core:security")
include(":core:network")
include(":core:database")
include(":core:download")
include(":core:ui")
include(":feature:home")
include(":feature:analyzer")
include(":feature:downloads")
include(":feature:library")
include(":feature:settings")
