plugins {
    alias(libs.plugins.run.waterfall)
}

tasks.runWaterfall {
    waterfallVersion("1.20")
}

dependencies {
    implementation(project(path = ":Bridge:ProtoWeaver:Bungee:API", configuration = "shadow"))
    implementation(project(path = ":Bridge:ProtoWeaver:Bungee:Core", configuration = "shadow"))
    implementation(libs.libby.bungee)

    compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
}

val pluginName = property("project.plugin.name") as String
val pluginVersion = property("project.plugin.version") as String
val pluginAuthor = property("project.plugin.author") as String

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
