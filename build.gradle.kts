plugins {
    kotlin("jvm") version "2.1.20" apply false
    kotlin("plugin.serialization") version "2.1.20" apply false
    kotlin("plugin.spring") version "2.1.20" apply false
    id("com.vanniktech.maven.publish") version "0.30.0" apply false
}

allprojects {
    group = "io.github.aaron-jang"
    version = "0.4.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "com.vanniktech.maven.publish")

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(17)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
        publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
        signAllPublications()

        coordinates(project.group.toString(), project.name, project.version.toString())

        pom {
            name.set(project.name)
            description.set("OpenDART API client for Kotlin/Java (JVM)")
            url.set("https://github.com/aaron-jang/opendart-reader")
            inceptionYear.set("2026")

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            developers {
                developer {
                    id.set("aaron-jang")
                    name.set("Aaron Jang")
                    url.set("https://github.com/aaron-jang")
                }
            }

            scm {
                url.set("https://github.com/aaron-jang/opendart-reader")
                connection.set("scm:git:git://github.com/aaron-jang/opendart-reader.git")
                developerConnection.set("scm:git:ssh://git@github.com/aaron-jang/opendart-reader.git")
            }
        }
    }
}
