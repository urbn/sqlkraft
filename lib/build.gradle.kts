import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("me.champeau.jmh") version "0.6.8"
    id("maven-publish")
    id("signing")
    `java-library`
}

group = "com.urbn.nu"
version = "1.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        apiVersion = "1.8"
        languageVersion = "1.8"
        jvmTarget = "17"
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.STANDARD_ERROR)

        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

jmh {
    warmupIterations.set(2)
    iterations.set(2)
    fork.set(2)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "sqlkraft"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("SqlKraft")
                description.set("Composable, parameterized SQL statements")
                url.set("https://github.com/urbn/sqlkraft")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/license/mit/")
                    }
                }
                developers {
                    developer {
                        name.set("Brad Morgan")
                        email.set("bmorgan1@urbn.com")
                        organization.set("URBN")
                        organizationUrl.set("https://www.urbn.com")
                    }
                }
                scm {
                    connection.set("scm:git@github.com:urbn/sqlkraft.git")
                    developerConnection.set("scm:git@github.com:urbn/sqlkraft.git")
                    url.set("https://github.com/urbn/sqlkraft")
                }
            }
        }
    }

    repositories {
        maven {
            name = "mavenCentral"
            val isSnapshot = version.toString().endsWith("SNAPSHOT")
            if (isSnapshot) {
                credentials(PasswordCredentials::class)
            }
            // Maven Central releases are manual for now
            val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
//            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
            val snapshotsRepoUrl = uri("https://central.sonatype.com/repository/maven-snapshots/")
            url = if (isSnapshot) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}