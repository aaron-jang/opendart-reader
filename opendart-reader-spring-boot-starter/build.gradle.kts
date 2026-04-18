plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

val springBootVersion = "3.4.5"

dependencies {
    api(project(":opendart-reader-core"))

    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
}
