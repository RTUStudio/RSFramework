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

val pluginName = property("plugin_name") as String
val pluginVersion = property("plugin_version") as String
val pluginAuthor = property("plugin_author") as String

tasks.named<ProcessResources>("processResources") {
    val props =
        mapOf(
            "name" to pluginName,
            "version" to pluginVersion,
            "author" to pluginAuthor,
        )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("bungee.yml") {
        expand(props)
    }
}
