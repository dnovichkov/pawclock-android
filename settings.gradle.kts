pluginManagement {
    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*android.*")
            }
        }
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "pawclock-android"

include(
    ":app",
    ":core:designsystem",
    ":core:model",
    ":core:calculator",
    ":core:database",
    ":core:datastore",
    ":core:domain",
    ":core:testing",
    ":feature:pets",
    ":feature:editor",
    ":feature:quickcalc",
    ":feature:settings",
)
