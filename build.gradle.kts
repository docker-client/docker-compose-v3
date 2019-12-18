import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.*

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion
val slf4jVersion = "1.7.29"
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
    kotlin("jvm") version "1.3.61"
    id("maven-publish")
    id("com.github.ben-manes.versions") version "0.27.0"
    id("com.jfrog.bintray") version "1.8.4"
    id("net.ossindex.audit") version "0.4.11"
    id("io.freefair.github.package-registry-maven-publish") version "4.1.6"
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    implementation("io.github.microutils:kotlin-logging:1.7.8")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3")

    implementation("org.yaml:snakeyaml:1.25")
    implementation("com.squareup.moshi:moshi:1.9.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.2")
    testImplementation("com.beust:klaxon:5.2")

    implementation("com.google.re2j:re2j:1.3")
//    implementation("com.github.fge:json-schema-validator:2.2.6")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.9")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.9")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.5.2")
}

val dependencyVersions = listOf(
        "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion",
        "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion",
        "org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion",
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion",
        "org.jetbrains.kotlin:kotlin-test:$kotlinVersion",
        "org.slf4j:slf4j-api:$slf4jVersion"
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
            includeEngines("spek2")
        }
    }

    bintrayUpload {
        dependsOn("build")
    }

    wrapper {
        gradleVersion = "6.0.1"
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
//            artifact(sourcesJar.get())
        }
    }
}

fun findProperty(s: String) = project.findProperty(s) as String?

rootProject.github {
    slug.set("${project.property("github.package-registry.owner")}/${project.property("github.package-registry.repository")}")
    username.set(System.getenv("GITHUB_ACTOR") ?: findProperty("github.package-registry.username"))
    token.set(System.getenv("GITHUB_TOKEN") ?: findProperty("github.package-registry.password"))
}

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
