import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.*

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion
rootProject.extra.set("artifactVersion", SimpleDateFormat("yyyy-MM-dd\'T\'HH-mm-ss").format(Date()))
rootProject.extra.set("bintrayDryRun", false)

buildscript {
    repositories {
        mavenLocal()
        jcenter()
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.3.21"
    `maven-publish`
    id("com.github.ben-manes.versions") version "0.20.0"
    id("com.jfrog.bintray") version "1.8.4"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenLocal()
    jcenter()
    maven { setUrl("http://dl.bintray.com/jetbrains/spek") }
    mavenCentral()
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compile("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    compile("io.github.microutils:kotlin-logging:1.6.24")
    compile("org.slf4j:slf4j-api:1.7.25")
    testRuntime("ch.qos.logback:logback-classic:1.2.3")

    compile("org.yaml:snakeyaml:1.23")
    compile("com.squareup.moshi:moshi:1.8.0")
    compile("com.squareup.moshi:moshi-kotlin:1.8.0")
    testCompile("com.beust:klaxon:5.0.2")

    compile("com.google.re2j:re2j:1.2")
//    compile("com.github.fge:json-schema-validator:2.2.6")

    testCompile("org.jetbrains.kotlin:kotlin-test")
    testCompile("org.jetbrains.spek:spek-api:1.2.1") {
        exclude("org.jetbrains.kotlin")
    }
    testRuntime("org.jetbrains.spek:spek-junit-platform-engine:1.2.1") {
        exclude("org.junit.platform")
        exclude("org.jetbrains.kotlin")
    }
    testRuntime("org.junit.platform:junit-platform-launcher:1.4.0")
}

val dependencyVersions = listOf(
        "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion",
        "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion",
        "org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion"
)

configurations.all {
    resolutionStrategy {
        failOnVersionConflict()
        force(dependencyVersions)
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType(Test::class.java) {
        useJUnitPlatform {
            includeEngines("spek")
        }
    }

    bintrayUpload {
        dependsOn("build")
    }

    register<Wrapper>("updateWrapper") {
        gradleVersion = "5.2.1"
        distributionType = Wrapper.DistributionType.ALL
    }
}
//compileTestKotlin {
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
//}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn("classes")
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

artifacts {
    add("archives", sourcesJar.get())
}

val publicationName = "dockerCompose"
publishing {
    publications {
        register(publicationName, MavenPublication::class) {
            groupId = "de.gesellix"
            artifactId = "docker-compose"
            version = rootProject.extra["artifactVersion"] as String
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}

fun findProperty(s: String) = project.findProperty(s) as String?

bintray {
    user = System.getenv()["BINTRAY_USER"] ?: findProperty("bintray.user")
    key = System.getenv()["BINTRAY_API_KEY"] ?: findProperty("bintray.key")
    setPublications(publicationName)
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "docker-utils"
        name = "docker-compose"
        desc = "A Docker compose v3 abstraction for the JVM"
        setLicenses("Apache-2.0")
        setLabels("docker", "compose", "stack", "deploy", "java")
        version.name = rootProject.extra["artifactVersion"] as String
        vcsUrl = "https://github.com/docker-client/docker-compose-v3.git"
    })
    dryRun = rootProject.extra["bintrayDryRun"] as Boolean
}
