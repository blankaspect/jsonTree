/*====================================================================*\

Gradle build script : JsonTransformer

\*====================================================================*/

// Plug-ins

plugins {
    java
}

//----------------------------------------------------------------------

// Functions

fun _path(vararg components : String): String =
        components.map { it.replace('/', File.separatorChar) }.joinToString(separator = File.separator)

fun _appSystemProperties() =
        System.getProperties()
                .filter { (key, _) -> (key is String) && key.startsWith("blankaspect.app.") }
                .mapKeys { it.key as String }

//----------------------------------------------------------------------

// Properties

val javaVersion = 17

val projectName = project.name

val mainClassName   = "uk.blankaspect.jsontransformer.JsonTransformerDemo"

val parentSourceDir = _path("..", "..", "src", "main", "java")

val buildDir    = layout.buildDirectory.get().getAsFile().toString()
val jarDir      = _path(buildDir, "bin")
val jarFilename = "jsonTransformer.jar"

//----------------------------------------------------------------------

// Repositories

repositories {
    mavenCentral()
}

//----------------------------------------------------------------------

// Dependencies

dependencies {
    val extLib_id_saxonHe      : String by project
    val extLib_version_saxonHe : String by project

    implementation("${extLib_id_saxonHe}:${extLib_version_saxonHe}")
}

//----------------------------------------------------------------------

// Compile

tasks.compileJava {
    options.release.set(javaVersion)
    options.sourcepath = files(parentSourceDir)
}

//----------------------------------------------------------------------

// Create executable JAR

tasks.jar {
    // Add contents of JAR files of dependencies
    from(
        configurations.runtimeClasspath.get()
                .filter { it.name.endsWith(".jar") }
                .map { zipTree(it).matching { exclude("META-INF/*", "META-INF/maven/**") } }
    )

    // Set properties of JAR
    destinationDirectory.set(file(jarDir))
    archiveFileName.set(jarFilename)
    manifest {
        attributes(
            "Application-Name" to projectName,
            "Main-Class"       to mainClassName
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

//----------------------------------------------------------------------

// Run main class

tasks.register<JavaExec>("runMain") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(mainClassName)

    systemProperties(_appSystemProperties())
}

//----------------------------------------------------------------------

// Run main class, writing output files to 'temp' directory

tasks.register<JavaExec>("runMainOutput") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(mainClassName)
    args = listOf("temp")

    systemProperties(_appSystemProperties())
}

//----------------------------------------------------------------------

// Run executable JAR

tasks.register<JavaExec>("runJar") {
    classpath = files(tasks.jar)

    systemProperties(_appSystemProperties())
}

//----------------------------------------------------------------------
