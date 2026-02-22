val pluginName = property("project.plugin.name") as String
val pluginVersion = property("project.plugin.version") as String
val pluginAuthor = property("project.plugin.author") as String
val apiVersion = property("project.server.apiVersion") as String

dependencies {
    implementation(project(":LightDI"))
    implementation(project(path = ":Bridge:ProtoWeaver:Bukkit:API", configuration = "shadow"))

    compileOnly(project(":Framework:API"))

    implementation(project(":Platform:Spigot"))

    compileOnly("io.papermc.paper:paper-api:$apiVersion-R0.1-SNAPSHOT")
}

tasks.named<ProcessResources>("processResources") {
    val props =
        mapOf(
            "name" to pluginName,
            "version" to pluginVersion,
            "author" to pluginAuthor,
        )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
