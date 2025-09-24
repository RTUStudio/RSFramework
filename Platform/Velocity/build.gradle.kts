plugins {
    id("xyz.jpenilla.run-velocity") version "2.3.1"
}

tasks.runVelocity {
    velocityVersion("3.4.0-SNAPSHOT")
}

dependencies {
    implementation(project(path = ":Library:ProtoWeaver:Velocity:API", configuration = "shadow"))
    implementation(project(path = ":Library:ProtoWeaver:Velocity:Core", configuration = "shadow"))

    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    implementation("com.alessiodp.libby:libby-velocity:2.0.0-SNAPSHOT")
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to (property("plugin_version") as String),
        "name" to (property("plugin_name") as String),
        "author" to (property("plugin_author") as String),
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("velocity-plugin.json") {
        expand(props)
    }
}
