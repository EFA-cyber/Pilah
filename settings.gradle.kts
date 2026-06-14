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
include(":core:datastore")
include(":core:network")
include(":feature:scan")
include(":feature:classification")
include(":feature:review")
include(":feature:foldering")
include(":feature:quarantine")
include(":feature:dashboard")
include(":cli")
