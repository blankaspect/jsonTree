/*====================================================================*\

Gradle build script : JsonXmlTest

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

val mainClassName   = "uk.blankaspect.jsonxmltest.JsonXmlTestApp"

val parentSourceDir = _path("..", "..", "src", "main", "java")

val buildDir    = layout.buildDirectory.get().getAsFile().toString()
val jarDir      = _path(buildDir, "bin")
val jarFilename = "jsonXmlTest.jar"

//----------------------------------------------------------------------

// Compile

tasks.compileJava {
    options.release.set(javaVersion)
    options.sourcepath = files(parentSourceDir)
}

//----------------------------------------------------------------------

// Create executable JAR

tasks.jar {
    destinationDirectory.set(file(jarDir))
    archiveFileName.set(jarFilename)
    manifest {
        attributes(
            "Application-Name" to projectName,
            "Main-Class"       to mainClassName
        )
    }
}

//----------------------------------------------------------------------

// Run main class

tasks.register<JavaExec>("runMain") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(mainClassName)

    systemProperties(_appSystemProperties())
}

//----------------------------------------------------------------------

// Run executable JAR

tasks.register<JavaExec>("runJar") {
    classpath = files(tasks.jar)

    systemProperties(_appSystemProperties())
}

//----------------------------------------------------------------------
