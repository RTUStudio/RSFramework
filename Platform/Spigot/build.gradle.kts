val apiVersion = property("project.server.apiVersion") as String
val pluginName = property("project.plugin.name") as String
val pluginVersion = property("project.plugin.version") as String
val pluginAuthor = property("project.plugin.author") as String

dependencies {
    implementation(project(":LightDI"))
    implementation(project(path = ":Bridge:Proxium:Bukkit", configuration = "shadow"))

    compileOnly(project(":Framework:API"))

    compileOnly("org.spigotmc:spigot-api:$apiVersion-R0.1-SNAPSHOT")

    implementation(libs.bstats)
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
