plugins {
    kotlin("jvm") version "2.3.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin:javalin:6.7.0")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass.set("MainKt")
}