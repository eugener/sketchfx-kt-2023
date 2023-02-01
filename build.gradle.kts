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

val jvmVersion by extra { 19 }

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
    jvmToolchain(jvmVersion)
}

application {
    mainClass.set("org.sketchfx.MainKt")
}

javafx {
    version = "$jvmVersion"
    modules("javafx.controls", "javafx.fxml")
}