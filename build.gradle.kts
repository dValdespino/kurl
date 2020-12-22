plugins {
    kotlin("multiplatform") version "1.4.20"
    id("maven-publish")
}

group = "com.github.darvld"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    explicitApi()

    mingwX64("native") {
        compilations.getByName("main").cinterops.create("libcurl")
        mavenPublication {
            artifactId = "ux"
        }
    }

    sourceSets {
        val nativeMain by getting
        val nativeTest by getting
    }
}
