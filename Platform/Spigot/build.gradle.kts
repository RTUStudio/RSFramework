val apiVersion = property("api_version") as String
val pluginName = property("plugin_name") as String
val pluginVersion = property("plugin_version") as String
val pluginAuthor = property("plugin_author") as String

dependencies {
    implementation(project(":Library:LightDI"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:API", configuration = "shadow"))

    compileOnly(project(":Framework:API"))

    compileOnly("org.spigotmc:spigot-api:$apiVersion-R0.1-SNAPSHOT")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.alessiodp.libby:libby-bukkit:2.0.0-SNAPSHOT")
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
    filesMatching("plugin.yml") {
        expand(props)
    }
}
