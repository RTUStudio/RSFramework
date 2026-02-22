plugins {
    alias(libs.plugins.run.velocity)
}

tasks.runVelocity {
    velocityVersion("3.4.0-SNAPSHOT")
}

dependencies {
    implementation(project(path = ":Bridge:ProtoWeaver:Velocity:API", configuration = "shadow"))
    implementation(project(path = ":Bridge:ProtoWeaver:Velocity:Core", configuration = "shadow"))

    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    implementation(libs.libby.velocity)
}

val pluginName = property("project.plugin.name") as String
val pluginVersion = property("project.plugin.version") as String
val pluginAuthor = property("project.plugin.author") as String

tasks.named<ProcessResources>("processResources") {
    val props =
        mapOf(
            "version" to pluginVersion,
            "name" to pluginName,
            "author" to pluginAuthor,
        )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("velocity-plugin.json") {
        expand(props)
    }
}
