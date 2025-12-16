plugins {
    id("io.papermc.paperweight.userdev")
}

val apiVersion = property("api_version") as String

dependencies {
    compileOnly("org.spigotmc:spigot-api:$apiVersion-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:$apiVersion-R0.1-SNAPSHOT")

    paperweight.paperDevBundle("$apiVersion-R0.1-SNAPSHOT")
    add("pluginRemapper", "net.fabricmc:tiny-remapper:0.10.4:fat")

    implementation(project(path = ":Library:ProtoWeaver:Common:API", configuration = "shadow"))
    implementation(project(path = ":Library:ProtoWeaver:Common:Core", configuration = "shadow"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:API", configuration = "shadow"))
}
