plugins {
    alias(libs.plugins.paperweight)
}

val apiVersion = property("project.server.apiVersion") as String

dependencies {
    compileOnly("org.spigotmc:spigot-api:$apiVersion-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:$apiVersion-R0.1-SNAPSHOT")

    paperweight.paperDevBundle("$apiVersion-R0.1-SNAPSHOT")
    add(
        "pluginRemapper",
        libs.tiny.remapper
            .get()
            .toString() + ":fat",
    )

    implementation(project(path = ":Bridge:ProtoWeaver:Common:API", configuration = "shadow"))
    implementation(project(path = ":Bridge:ProtoWeaver:Common:Core", configuration = "shadow"))
    implementation(project(path = ":Bridge:ProtoWeaver:Bukkit:API", configuration = "shadow"))
}
