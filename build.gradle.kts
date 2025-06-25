plugins {
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("maven-publish")
}

val minecraftVersion: String by project
val yarnMappings: String by project
val loaderVersion: String by project
val fabricVersion: String by project

base {
    archivesName.set(project.property("archives_base_name") as String)
}

version = project.property("mod_version") as String
group = "kz.denver"

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    include("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
}

tasks.processResources {
    inputs.property("version", project.version)
    
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.release.set(17)
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}