plugins {
    id("xyz.jpenilla.run-waterfall") version "2.3.1"
}

tasks.runWaterfall {
    waterfallVersion("1.20")
}

dependencies {
    implementation(project(path = ":Library:ProtoWeaver:Bungee:API", configuration = "shadow"))
    implementation(project(path = ":Library:ProtoWeaver:Bungee:Core", configuration = "shadow"))
    implementation("com.alessiodp.libby:libby-bungee:2.0.0-SNAPSHOT")

    compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
}

val plugin_name: String by project
val plugin_version: String by project
val plugin_author: String by project

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to plugin_version,
        "name" to plugin_name,
        "author" to plugin_author,
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("bungee.yml") {
        expand(props)
    }
}
