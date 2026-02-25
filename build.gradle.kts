plugins {
    java
    `maven-publish`
    alias(libs.plugins.freefair.lombok)
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight) apply false
    alias(libs.plugins.spotless) apply false
}

group = "kr.rtustudio"
val pluginVersion = property("project.plugin.version") as String
val pluginName = property("project.plugin.name") as String
val javaVersionProp = property("project.java.version") as String
version = pluginVersion

val catalog = the<VersionCatalogsExtension>().named("libs")

allprojects {

    apply(plugin = "java")
    apply(plugin = "io.freefair.lombok")
    if (project.name != "Configurate") {
        apply(plugin = "com.gradleup.shadow")
    }
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
        compileOnly(catalog.findLibrary("quartz").get())

        compileOnly(catalog.findLibrary("adventure-minimessage").get())
        compileOnly(catalog.findLibrary("adventure-serializer-gson").get())
        compileOnly(catalog.findLibrary("gson").get())
        compileOnly(catalog.findLibrary("guava").get())
        compileOnly(catalog.findLibrary("commons-lang3").get())
        compileOnly(catalog.findLibrary("slf4j").get())

        compileOnly(catalog.findLibrary("lombok").get())
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

    if (project.name != "Configurate") {
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
}

dependencies {
    implementation(project(":Framework"))
    implementation(project(":Platform:Bungee"))
    implementation(project(":Platform:Velocity"))

    implementation(libs.libby.bukkit)
    implementation(libs.libby.bungee)
    implementation(libs.libby.velocity)
    implementation(libs.adventure.platform.bukkit)
    implementation(libs.adventure.serializer.gson)
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
            version = property("project.plugin.version") as String
            from(components["shadow"])
        }
    }
}
