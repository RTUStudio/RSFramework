val api_version: String by project
val plugin_name: String by project
val plugin_version: String by project
val plugin_author: String by project

dependencies {
    implementation(project(":Library:LightDI"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:API", configuration = "shadow"))

    compileOnly(project(":Framework:API"))

    compileOnly("org.spigotmc:spigot-api:${api_version}-R0.1-SNAPSHOT")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.alessiodp.libby:libby-bukkit:2.0.0-SNAPSHOT")
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "name" to plugin_name,
        "version" to plugin_version,
        "author" to plugin_author,
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
