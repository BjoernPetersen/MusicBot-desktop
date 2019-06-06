import com.diffplug.spotless.LineEnding
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.diffplug.gradle.spotless") version Plugin.SPOTLESS

    id("com.github.ben-manes.versions") version Plugin.VERSIONS

    kotlin("jvm") version Plugin.KOTLIN
    application
    idea

    id("org.jetbrains.dokka") version Plugin.DOKKA
}

group = "com.github.bjoernpetersen"
version = "0.18.0-SNAPSHOT"

repositories {
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots") {
        mavenContent {
            snapshotsOnly()
        }
    }
}

application {
    mainClassName = "net.bjoernpetersen.deskbot.view.DeskBot"
}

idea {
    module {
        isDownloadJavadoc = true
    }
}

spotless {
    kotlin {
        ktlint()
        lineEndings = LineEnding.UNIX
        endWithNewline()
    }
    kotlinGradle {
        ktlint()
        lineEndings = LineEnding.UNIX
        endWithNewline()
    }
    format("markdown") {
        target("**/*.md")
        lineEndings = LineEnding.UNIX
        endWithNewline()
    }
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.MINUTES)
}

tasks {
    "dokka"(DokkaTask::class) {
        outputFormat = "html"
        outputDirectory = "$buildDir/kdoc"
    }

    @Suppress("UNUSED_VARIABLE")
    val dokkaJavadoc by creating(DokkaTask::class) {
        outputFormat = "javadoc"
        outputDirectory = "$buildDir/javadoc"
    }

    "compileKotlin"(KotlinCompile::class) {
        kotlinOptions.jvmTarget = "1.8"
    }

    "compileTestKotlin"(KotlinCompile::class) {
        kotlinOptions.jvmTarget = "1.8"
    }

    "processResources"(ProcessResources::class) {
        filesMatching("**/version.properties") {
            filter {
                it.replace("%APP_VERSION%", version.toString())
            }
        }
    }

    "test"(Test::class) {
        useJUnitPlatform()
    }

    withType(Jar::class) {
        from(project.projectDir) {
            include("LICENSE")
        }
    }
}

dependencies {
    runtime(
        group = "org.slf4j",
        name = "slf4j-simple",
        version = Lib.SLF4J
    )
    implementation(
        group = "com.github.bjoernpetersen",
        name = "musicbot",
        version = Lib.MUSICBOT
    ) {
        isChanging = Lib.MUSICBOT.contains("SNAPSHOT")
    }
    implementation(
        group = "org.jetbrains.kotlinx",
        name = "kotlinx-coroutines-javafx",
        version = Lib.KOTLIN_COROUTINES
    )

    // Vertx
    implementation(group = "io.vertx", name = "vertx-web-api-contract", version = Lib.VERTX)
    implementation(group = "io.vertx", name = "vertx-lang-kotlin", version = Lib.VERTX) {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation(group = "io.vertx", name = "vertx-lang-kotlin-coroutines", version = Lib.VERTX) {
        exclude(group = "org.jetbrains.kotlin")
    }

    // JavaFX
    implementation("org.controlsfx:controlsfx:${Lib.CONTROLS_FX}")

    implementation(
        group = "com.fasterxml.jackson.core",
        name = "jackson-databind",
        version = Lib.JACKSON
    )
    implementation(
        group = "com.fasterxml.jackson.module",
        name = "jackson-module-kotlin",
        version = Lib.JACKSON
    )
    implementation(
        group = "com.fasterxml.jackson.dataformat",
        name = "jackson-dataformat-yaml",
        version = Lib.JACKSON
    )

    testRuntime(
        group = "org.junit.jupiter",
        name = "junit-jupiter-engine",
        version = Lib.JUNIT
    )
    testImplementation(
        group = "org.junit.jupiter",
        name = "junit-jupiter-api",
        version = Lib.JUNIT
    )
    testImplementation(
        group = "name.falgout.jeffrey.testing.junit5",
        name = "guice-extension",
        version = Lib.JUNIT_GUICE
    )
    testImplementation(group = "io.mockk", name = "mockk", version = Lib.MOCK_K)
    testImplementation(group = "org.assertj", name = "assertj-core", version = Lib.ASSERT_J)
}
