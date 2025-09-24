plugins {
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

val apiVersion = property("api_version") as String
val bukkitVersion = property("bukkit_version") as String
val pluginName = property("plugin_name") as String
val pluginVersion = property("plugin_version") as String

listOf("API", "Core").forEach { name ->
    project(":Framework:$name") {
        dependencies {
            implementation(project(":Library:LightDI"))

            compileOnly(project(":Library:ProtoWeaver:Common:API"))
            compileOnly(project(":Library:ProtoWeaver:Bukkit:API"))

            compileOnly("org.spigotmc:spigot-api:$apiVersion-R0.1-SNAPSHOT")
            compileOnly("io.papermc.paper:paper-api:$apiVersion-R0.1-SNAPSHOT")

            implementation("net.kyori:adventure-platform-bukkit:4.3.4")
            implementation("de.tr7zw:item-nbt-api:2.15.2-SNAPSHOT")

            compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.3-beta-14")

            compileOnly("io.th0rgal:oraxen:1.189.0")

            compileOnly("net.Indyuce:MMOItems-API:6.10.1-SNAPSHOT")
            compileOnly("io.lumine:MythicLib-dist:1.7.1-SNAPSHOT")

            compileOnly("com.nexomc:nexo:1.8.0")

            compileOnly("com.willfp:EcoItems:5.62.0")
            compileOnly("com.willfp:eco:6.75.2")
            compileOnly("com.willfp:libreforge:4.64.1")

            implementation("org.spongepowered:configurate-yaml:4.2.0-GeyserMC-SNAPSHOT")
        }
    }
}

dependencies {
    implementation(project(":Framework:API"))
    implementation(project(":Framework:Core"))
}

tasks.jar {
    finalizedBy(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier.set(null as String?)
    archiveBaseName.set(pluginName)
    archiveVersion.set(pluginVersion)
}

tasks.runServer {
    minecraftVersion(bukkitVersion)
}
