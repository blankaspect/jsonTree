/*====================================================================*\

Gradle build script : jsonTree

\*====================================================================*/

// Plug-ins

plugins {
    java
}

//----------------------------------------------------------------------

// Functions

fun _path(vararg components : String): String =
        components.map { it.replace('/', File.separatorChar) }.joinToString(separator = File.separator)

//----------------------------------------------------------------------

// Properties

val javaVersion = 17

val projectName = project.name

val buildDir    = layout.buildDirectory.get().getAsFile().toString()
val jarDir      = _path(buildDir, "bin")
val jarFilename = "${projectName}.jar"

//----------------------------------------------------------------------

// Compile

tasks.compileJava {
    options.release.set(javaVersion)
}

//----------------------------------------------------------------------

// Create JAR

tasks.jar {
    destinationDirectory.set(file(jarDir))
    archiveFileName.set(jarFilename)
}

//----------------------------------------------------------------------
