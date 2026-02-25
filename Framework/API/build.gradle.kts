plugins {
    `maven-publish`
}

val pluginVersion = property("project.plugin.version") as String

dependencies {
    implementation(project(":Configurate"))

    implementation(project(path = ":Bridge:Proxium:Common:API", configuration = "shadow"))
    implementation(project(":Bridge:Common"))

    implementation(project(":Storage:Common"))

    compileOnly(libs.libby.bukkit)

    compileOnly(libs.snappy)
    compileOnly(libs.fastutil)

    compileOnly(libs.placeholderapi)
    compileOnly(libs.packetevents)

    // configurate-yaml is provided transitively via :Configurate module
}

// API Build
tasks.shadowJar {
    archiveClassifier.set(null as String?)
    archiveBaseName.set("api")
    archiveVersion.set(pluginVersion)

    doLast {
        copy {
            from(archiveFile.get().asFile)
            into(file("$rootDir/builds/api"))
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.codemc.io/repository/rtustudio/")

            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("api") {
            groupId = "kr.rtustudio"
            artifactId = "framework-api"
            version = pluginVersion

            from(components["shadow"])
        }
    }
}
