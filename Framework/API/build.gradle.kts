plugins {
    `maven-publish`
}

val pluginVersion = property("project.plugin.version") as String

dependencies {
    implementation(project(path = ":Bridge:ProtoWeaver:Bukkit:API", configuration = "shadow"))
    implementation(project(":Bridge:Common"))
    compileOnly(project(":Bridge:Redisson"))

    compileOnly(project(":Storage:Common"))

    compileOnly(libs.libby.bukkit)

    compileOnly(libs.snappy)
    compileOnly(libs.fastutil)

    compileOnly(libs.placeholderapi)
    compileOnly(libs.packetevents)

    implementation(libs.configurate.yaml)
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
