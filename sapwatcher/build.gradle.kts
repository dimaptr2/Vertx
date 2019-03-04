plugins {
    java
    idea
    application
}

group = "ru.velkomfood.tv.sap.watcher"
version = "8.0.1"
val sapJavaConnector = "/usr/sap/JCo/sapjco3.jar"
val vertxVersion = "3.6.3"

application {
    mainClassName = "${group}.Pusher"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compileOnly(fileTree(sapJavaConnector))
    compile("io.vertx", "vertx-web", vertxVersion)
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}


// The jar generation and the manifest creation with the attributes
tasks.jar {

    manifest {
        attributes(
                "Implementation-Title" to "SAP TV Vertx service",
                "Implementation-Version" to project.version,
                "Class-Path" to sapJavaConnector,
                "Main-Class" to application.mainClassName
        )
    }
// For the Fat Jar creation
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations
                .runtimeClasspath
                .get()
                .filter { it.name.endsWith("jar") && !it.name.startsWith("sap")}.map { zipTree(it) }
    })

}

// Copy an additional JVM based and not JVM based dependencies into building directory
tasks.register<Copy>("copySapLibrary") {
    from("/usr/sap/JCo") {
        include("sapidoc3.jar", "sapjco3.jar", "libsapjco3.so")
    }
    into("build/libs")
}

// Refresh the Git directory of this project
tasks.register<Delete>("deleteGitDir") {
    delete("/home/dpetrov/github.com/Vertx/${project.name}")
}

// Prepare to the transfer for Github
tasks.register<Copy>("prepareToGit") {

    from("${projectDir}") {
        include(
                "**/*.htm",
                "**/*.html",
                "**/*.java",
                "**/*.js",
                "**/*.json",
                "**/*.css",
                "**/*.gradle",
                "**/*.kts",
                "**/*.properties",
                "**/*.txt",
                "**/*.xml"
        )
        exclude(".gradle", ".idea", ".vertx")
    }

    into("/home/dpetrov/github.com/Vertx/${project.name}")
}
