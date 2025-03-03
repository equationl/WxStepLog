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
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "WxStepLog"
include(":app")
include(":assists")
include(":aiapi")
include(":common")

val includePaidModule = providers.gradleProperty("include.paid.module").orNull?.toBoolean() ?: false
if (includePaidModule) {
    include(":aipro")
}
