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

rootProject.name = "Pilah"

include(":app")
include(":core:model")
include(":core:common")
include(":core:database")
include(":core:designsystem")
include(":core:permissions")
include(":feature:scan")
include(":feature:classification")
