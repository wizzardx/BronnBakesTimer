// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false

    // Support checking for newer versions of dependencies. eg, you can now use this command to
    // check for newer dependency versions: ./gradlew dependencyUpdates
    id("com.github.ben-manes.versions") version "0.50.0"

    // ktlint integration in Gradle. eg, you can now use this command to lint:
    // ./gradlew ktlintCheck, and ./gradlew ktlintFormat to reformat code
    id("org.jlleitschuh.gradle.ktlint") version "12.0.3" apply false

    // detetk command-line integration with Gradle. Run this command to do lints:
    // ./gradlew detekt
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
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

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.0.1")
        debug.set(true)
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
    }
}

detekt {
    toolVersion = "1.23.4"
    source.setFrom(files("app/src/main/java"))
    config.from(files("$rootDir/config/detekt.yml"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
    }
}
