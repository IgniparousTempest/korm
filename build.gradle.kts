import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    maven
    kotlin("jvm") version "1.2.71"
}

group = "com.github.igniparoustempest"
version = "v0.3.1"

val dataFactoryVersion = "0.8"
val junit5Version = "5.3.1"
val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion
val mockitoVersion = "2.22.0"
val sqliteVersion = "3.23.1"


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.xerial:sqlite-jdbc:$sqliteVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.fluttercode.datafactory:datafactory:$dataFactoryVersion")
}

tasks {
    "test"(Test::class) {
        useJUnitPlatform()
    }
}