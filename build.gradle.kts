import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.*

plugins {
  kotlin("jvm") version "2.1.0"
  id("maven-publish")
  id("signing")
  id("com.github.ben-manes.versions") version "0.51.0"
  id("net.ossindex.audit") version "0.4.11"
  id("io.freefair.maven-central.validate-poms") version "8.11"
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

repositories {
  mavenCentral()
}

dependencies {
  constraints {
    implementation("org.slf4j:slf4j-api") {
      version {
        strictly("[1.7,3)")
        prefer("2.0.16")
      }
    }
    listOf(
      "org.jetbrains.kotlin:kotlin-reflect",
      "org.jetbrains.kotlin:kotlin-scripting-jvm",
      "org.jetbrains.kotlin:kotlin-stdlib",
      "org.jetbrains.kotlin:kotlin-stdlib-jdk7",
      "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
      "org.jetbrains.kotlin:kotlin-stdlib-common",
      "org.jetbrains.kotlin:kotlin-test"
    ).forEach {
      implementation(it) {
        version {
          strictly("[1.6,3)")
          prefer("2.1.0")
        }
      }
    }
    listOf(
      "com.squareup.moshi:moshi",
      "com.squareup.moshi:moshi-kotlin"
    ).forEach {
      implementation(it) {
        version {
          strictly("[1.12.0,2)")
          prefer("1.15.2")
        }
      }
    }
    listOf(
      "com.squareup.okio:okio",
      "com.squareup.okio:okio-jvm"
    ).forEach {
      implementation(it) {
        version {
          strictly("[3,4)")
          prefer("3.9.1")
        }
      }
    }
  }
  implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
  implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.0")

  implementation("io.github.microutils:kotlin-logging:3.0.5")
  implementation("org.slf4j:slf4j-api:2.0.16")
  testRuntimeOnly("ch.qos.logback:logback-classic:1.3.14")

  implementation("org.yaml:snakeyaml:2.3")
  implementation("com.squareup.moshi:moshi:1.15.2")
  implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
  testImplementation("com.beust:klaxon:5.5")

  implementation("com.google.re2j:re2j:1.7")
//    implementation("com.github.fge:json-schema-validator:2.2.6")

  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.1.0")
  testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}

val dependencyVersions = listOf(
  "net.bytebuddy:byte-buddy:1.15.11",
  "net.bytebuddy:byte-buddy-agent:1.15.11",
  "org.jetbrains:annotations:26.0.1",
  "org.opentest4j:opentest4j:1.3.0",
)

val dependencyGroupVersions = mapOf(
  "org.junit.jupiter" to "5.11.0",
  "org.junit.platform" to "1.11.0",
)

configurations.all {
  resolutionStrategy {
    failOnVersionConflict()
    force(dependencyVersions)
    eachDependency {
      val forcedVersion = dependencyGroupVersions[requested.group]
      if (forcedVersion != null) {
        useVersion(forcedVersion)
      }
    }
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
}

tasks {
  withType<JavaCompile> {
    options.encoding = "UTF-8"
  }

  withType<KotlinCompile> {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
    }
  }

  withType<Test> {
    useJUnitPlatform()
  }
}

val javadocJar by tasks.registering(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("javadoc")
  from(tasks.javadoc)
}

val sourcesJar by tasks.registering(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allSource)
}

artifacts {
  add("archives", sourcesJar.get())
  add("archives", javadocJar.get())
}

fun findProperty(s: String) = project.findProperty(s) as String?

val isSnapshot = project.version == "unspecified"
val artifactVersion = if (!isSnapshot) project.version as String else SimpleDateFormat("yyyy-MM-dd\'T\'HH-mm-ss").format(Date())!!
val publicationName = "dockerCompose"
publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/${property("github.package-registry.owner")}/${property("github.package-registry.repository")}")
      credentials {
        username = System.getenv("GITHUB_ACTOR") ?: findProperty("github.package-registry.username")
        password = System.getenv("GITHUB_TOKEN") ?: findProperty("github.package-registry.password")
      }
    }
  }
  publications {
    register(publicationName, MavenPublication::class) {
      pom {
        name.set("docker-compose")
        description.set("A Docker compose v3 abstraction for the JVM")
        url.set("https://github.com/docker-client/docker-compose-v3")
        licenses {
          license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
          }
        }
        developers {
          developer {
            id.set("gesellix")
            name.set("Tobias Gesellchen")
            email.set("tobias@gesellix.de")
          }
        }
        scm {
          connection.set("scm:git:github.com/docker-client/docker-compose-v3.git")
          developerConnection.set("scm:git:ssh://github.com/docker-client/docker-compose-v3.git")
          url.set("https://github.com/docker-client/docker-compose-v3")
        }
      }
      artifactId = "docker-compose"
      version = artifactVersion
      from(components["java"])
      artifact(sourcesJar.get())
      artifact(javadocJar.get())
    }
  }
}

signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications[publicationName])
}

nexusPublishing {
  repositories {
    if (!isSnapshot) {
      sonatype {
        // 'sonatype' is pre-configured for Sonatype Nexus (OSSRH) which is used for The Central Repository
        stagingProfileId.set(System.getenv("SONATYPE_STAGING_PROFILE_ID") ?: findProperty("sonatype.staging.profile.id")) //can reduce execution time by even 10 seconds
        username.set(System.getenv("SONATYPE_USERNAME") ?: findProperty("sonatype.username"))
        password.set(System.getenv("SONATYPE_PASSWORD") ?: findProperty("sonatype.password"))
      }
    }
  }
}
