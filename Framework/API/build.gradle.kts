plugins {
    `maven-publish`
}

dependencies {
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:API", configuration = "shadow"))

    compileOnly("com.alessiodp.libby:libby-bukkit:2.0.0-SNAPSHOT")

    compileOnly("com.zaxxer:HikariCP:7.0.2")
    compileOnly("org.mongodb:mongodb-driver-sync:5.5.1")
    compileOnly("com.mysql:mysql-connector-j:9.4.0")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.5")
//    compileOnly("org.xerial:sqlite-jdbc:3.45.3.0")
//    compileOnly("org.postgresql:postgresql:42.7.3")

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.dmulloy2:ProtocolLib:5.1.0")

    implementation("org.spongepowered:configurate-yaml:4.2.0-GeyserMC-SNAPSHOT")
}

// API Build
tasks.shadowJar {
    archiveClassifier.set(null as String?)
    archiveBaseName.set("api")
    archiveVersion.set(property("plugin_version") as String)

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
            version = property("plugin_version") as String

            from(components["shadow"])
        }
    }
}
