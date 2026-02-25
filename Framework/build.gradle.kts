plugins {
    alias(libs.plugins.run.paper)
}

val apiVersion = property("project.server.apiVersion") as String
val bukkitVersion = property("project.server.bukkitVersion") as String
val pluginName = property("project.plugin.name") as String
val pluginVersion = property("project.plugin.version") as String
val catalog = the<VersionCatalogsExtension>().named("libs")

listOf("API", "Core").forEach { name ->
    project(":Framework:$name") {
        dependencies {
            implementation(project(":LightDI"))

            compileOnly(project(":Bridge:Common"))
            compileOnly(project(":Bridge:Redisson"))
            compileOnly(project(":Bridge:Proxium:Common:API"))
            compileOnly(project(":Bridge:Proxium:Common:Core"))
            compileOnly(project(":Bridge:Proxium:Bukkit"))

            compileOnly("org.spigotmc:spigot-api:$apiVersion-R0.1-SNAPSHOT")
            compileOnly("io.papermc.paper:paper-api:$apiVersion-R0.1-SNAPSHOT")

            implementation(catalog.findLibrary("adventure-platform-bukkit").get())
            implementation(catalog.findLibrary("nbt-api").get())

            compileOnly(catalog.findLibrary("itemsadder").get())

            compileOnly(catalog.findLibrary("oraxen").get())

            compileOnly(catalog.findLibrary("mmoitems").get())
            compileOnly(catalog.findLibrary("mythiclib").get())

            compileOnly(catalog.findLibrary("nexo").get())

            compileOnly(catalog.findLibrary("ecoitems").get())
            compileOnly(catalog.findLibrary("eco").get())
            compileOnly(catalog.findLibrary("libreforge").get())

            implementation(catalog.findLibrary("configurate-yaml").get())
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
