// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false

    // Support checking for newer versions of dependencies:
    id("com.github.ben-manes.versions") version "0.50.0"
}

// Support checking for newer versions of dependencies:
tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
    resolutionStrategy {
        componentSelection {
            all {
                val rejectedKeywords = listOf("alpha", "beta", "rc", "m", "snapshot")
                for (keyword in rejectedKeywords) {
                    if (candidate.version.contains(keyword, ignoreCase = true)) {
                        reject("Version contains non-stable keyword: $keyword")
                        break
                    }
                }
            }
        }
    }
}
