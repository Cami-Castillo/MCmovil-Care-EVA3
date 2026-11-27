pluginManagement { repositories { google();mavenCentral();gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS);repositories { google();mavenCentral() } }
rootProject.name="MCmovilCare-EVA4"
include(":app",":logic")
project(":logic").projectDir=file("../logic")
