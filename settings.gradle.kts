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
        // SI ESTA LÍNEA FALTA O ESTÁ MAL ESCRITA, EL MQTT FALLARÁ SIEMPRE:
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ConectaMobile" // (Sin espacios preferiblemente, pero así funciona)
include(":app")