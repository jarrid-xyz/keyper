import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin library project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.8/userguide/building_java_projects.html in the Gradle documentation.
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.jvm)

    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    kotlin("plugin.serialization") version "2.0.0"

    application

    // Apply the Dokka plugin for generating documentation.
    id("org.jetbrains.dokka") version "1.9.20"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api(libs.commons.math3)

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(libs.guava)

    implementation(kotlin("stdlib"))

    implementation("com.hashicorp:cdktf:0.20.7")
    implementation("com.hashicorp:cdktf-provider-google:13.24.0")
    implementation("software.constructs:constructs:10.3.0")
    implementation("com.charleskorn.kaml:kaml:0.60.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("io.klogging:klogging-jvm:0.5.14")
    implementation("com.github.ajalt.clikt:clikt:4.2.2")
    implementation("com.github.f4b6a3:uuid-creator:5.3.7")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
    implementation("com.google.cloud:google-cloud-kms:2.49.0")

    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("com.google.jimfs:jimfs:1.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0-M2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.0-M2")

}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest("1.9.22")
        }
    }
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.9.20")
    }
}

tasks {
    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(
            listOf(
                "compileJava",
                "compileKotlin",
                "processResources"
            )
        ) // We need this for Gradle optimization to work
        archiveClassifier.set("standalone") // Naming the jar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to "jarrid.keyper.cli.MainKt")) } // Provided we set it up in the application plugin configuration
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA") // Exclude signature files
    }
    build {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }

    dokkaHtml {
        outputDirectory.set(file("build/dokka"))
    }

    withType<DokkaTask>().configureEach {
        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            footerMessage = "(c) 2024 Jarrid"
        }
        outputDirectory.set(file("$rootDir/mkdocs/kdoc"))
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("MainKt")
}
