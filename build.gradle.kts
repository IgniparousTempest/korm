import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.2.71"
    java
    jacoco
    maven
}

group = "com.github.igniparoustempest"
version = "v0.5.0"

val dataFactoryVersion = "0.8"
val junit5Version = "5.3.1"
val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion
val mockitoVersion = "2.22.0"
val sqliteVersion = "3.23.1"

jacoco {
    toolVersion = "0.8.2"
}

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

    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        classifier = "sources"
        from(sourceSets["main"].allSource)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
        classifier = "javadoc"
        from(tasks["javadoc"])
    }

    artifacts {
        add("archives", sourcesJar)
        add("archives", javadocJar)
    }

    val codeCoverageReport by creating(JacocoReport::class) {
        executionData(fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec"))

        subprojects.onEach {
            sourceSets(it.sourceSets["main"])
        }

        reports {
            sourceDirectories =  files(sourceSets["main"].allSource.srcDirs)
            classDirectories =  files(sourceSets["main"].output)
            xml.isEnabled = true
            xml.destination = File("$buildDir/reports/jacoco/report.xml")
            html.isEnabled = true
            csv.isEnabled = false
        }

        dependsOn("test")
    }
}