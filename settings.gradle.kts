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

// 移除dependencyResolutionManagement块，因为我们使用传统方式在根build.gradle.kts中定义repositories

rootProject.name = "My Application"
include(":app")
