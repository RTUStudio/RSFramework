plugins {
    java
    `maven-publish`
    id("io.freefair.lombok") version "8.14.2"
    id("com.gradleup.shadow") version "9.0.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18" apply false
    id("com.diffplug.spotless") version "7.2.1" apply false
}

group = "kr.rtustudio"
val pluginVersion = property("plugin_version") as String
val pluginName = property("plugin_name") as String
val javaVersionProp = property("java_version") as String
version = pluginVersion

allprojects {

    apply(plugin = "java")
    apply(plugin = "io.freefair.lombok")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "com.diffplug.spotless")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://mvnrepository.com/artifact/org.projectlombok/lombok")
        maven("https://repo.alessiodp.com/snapshots/") { name = "AlessioDP" }
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") { name = "spigotmc-repo" }
        maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
        maven("https://oss.sonatype.org/content/groups/public/") { name = "sonatype" }
        maven("https://repo.codemc.io/repository/maven-public/") { name = "CodeMC" }
        maven("https://repo.codemc.io/repository/maven-releases/") { name = "CodeMC" }

        maven("https://repo.viaversion.com")
        maven("https://repo.oraxen.com/releases")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://repo.nexomc.com/releases/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.essentialsx.net/releases") { name = "essentialsx" }
        maven("https://repo.opencollab.dev/maven-snapshots") { name = "GeyserMC Fork of SpongePowered/Configurate" }
    }

    dependencies {
        compileOnly("org.quartz-scheduler:quartz:2.5.0")

        compileOnly("net.kyori:adventure-text-minimessage:4.24.0")
        compileOnly("net.kyori:adventure-text-serializer-gson:4.24.0")
        compileOnly("com.google.code.gson:gson:2.13.1")
        compileOnly("com.google.guava:guava:33.4.8-jre")
        compileOnly("org.apache.commons:commons-lang3:3.18.0")
        compileOnly("org.xerial.snappy:snappy-java:1.1.10.8")
        compileOnly("org.slf4j:slf4j-api:2.0.17")

        compileOnly("org.projectlombok:lombok:1.18.38")
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
        }
        java {
            target("src/**/*.java")
            googleJavaFormat("1.22.0").aosp()
            removeUnusedImports()
            importOrder("", "java", "javax", "org", "com")
            trimTrailingWhitespace()
            endWithNewline()
        }

        format("gradle") {
            target("**/*.gradle.kts")
            trimTrailingWhitespace()
            leadingTabsToSpaces(4)
            endWithNewline()
        }
    }

    java {
        val javaVersion = JavaVersion.toVersion(javaVersionProp)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        if (JavaVersion.current() < javaVersion) {
            toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersionProp.toInt()))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(javaVersionProp.toInt())
    }

    tasks.jar {
        finalizedBy(tasks.shadowJar)
    }

    tasks.shadowJar {
        exclude("META-INF/**")
        exclude("classpath.index")

        relocate("com.alessiodp.libby", "kr.rtustudio.libby")
        relocate("net.kyori.adventure.platform.bukkit", "kr.rtustudio.framework.bukkit.kyori")
        relocate("de.tr7zw.changeme.nbtapi", "kr.rtustudio.framework.bukkit.api.nbt")
        relocate("org.spongepowered.configurate", "kr.rtustudio.configurate")
        relocate("org.bstats", "kr.rtustudio.framework.bstats")
    }
}

dependencies {
    implementation(project(":Framework"))
    implementation(project(":Platform:Bungee"))
    implementation(project(":Platform:Velocity"))

    implementation("com.alessiodp.libby:libby-bukkit:2.0.0-SNAPSHOT")
    implementation("com.alessiodp.libby:libby-bungee:2.0.0-SNAPSHOT")
    implementation("com.alessiodp.libby:libby-velocity:2.0.0-SNAPSHOT")
    implementation("net.kyori:adventure-platform-bukkit:4.4.1")

    compileOnly("org.projectlombok:lombok:1.18.38")
    add("annotationProcessor", "org.projectlombok:lombok:1.18.38")
}

// Plugin Build
tasks.shadowJar {
    file("$rootDir/builds").delete()

    archiveClassifier.set(null as String?)
    archiveBaseName.set(pluginName)
    archiveVersion.set(pluginVersion)

    doLast {
        copy {
            from(archiveFile.get().asFile)
            into(file("$rootDir/builds/plugin"))
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
        create<MavenPublication>("plugin") {
            groupId = "kr.rtustudio"
            artifactId = "framework-plugin"
            version = property("plugin_version") as String
            from(components["shadow"])
        }
    }
}
