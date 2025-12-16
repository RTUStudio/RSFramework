plugins {
    `maven-publish`
}

val pluginVersion = property("plugin_version") as String

dependencies {
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:API", configuration = "shadow"))

    compileOnly("com.alessiodp.libby:libby-bukkit:2.0.0-SNAPSHOT")

    compileOnly("com.zaxxer:HikariCP:7.0.2")
    compileOnly("org.mongodb:mongodb-driver-sync:5.5.1")
    compileOnly("com.mysql:mysql-connector-j:9.4.0")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.5")

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.retrooper:packetevents-spigot:2.10.1")

    implementation("org.spongepowered:configurate-yaml:4.2.0-GeyserMC-SNAPSHOT")
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
