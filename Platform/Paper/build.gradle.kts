val pluginName = property("plugin_name") as String
val pluginVersion = property("plugin_version") as String
val pluginAuthor = property("plugin_author") as String
val apiVersion = property("api_version") as String

dependencies {
    implementation(project(":Library:LightDI"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:API", configuration = "shadow"))

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
