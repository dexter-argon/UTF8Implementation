import org.gradle.internal.declarativedsl.parsing.main

plugins {
    id("application")
}

group = "com.utf8"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass = "com.utf8.Main"
}

tasks.test {
    useJUnitPlatform()
}