buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://nexus.nee.com/repository/maven-public/")
    }
}

plugins {
    kotlin("jvm") version "1.8.0"
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "org.sketchfx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://nexus.nee.com/repository/maven-public/")
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(18)
}

application {
    mainClass.set("org.sketchfx.MainKt")
}

javafx() {
    version = "19"
    modules("javafx.controls", "javafx.fxml")
}