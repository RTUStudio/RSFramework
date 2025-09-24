dependencies {
    implementation(project(":Library:LightDI"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:API", configuration = "shadow"))

    compileOnly(project(":Framework:API"))

    implementation(project(":Platform:Spigot"))

    val bukkit_version: String by project
    compileOnly("io.papermc.paper:paper-api:${bukkit_version}-R0.1-SNAPSHOT")
}

val plugin_name: String by project
val plugin_version: String by project
val plugin_author: String by project

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "name" to plugin_name,
        "version" to plugin_version,
        "author" to plugin_author,
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
