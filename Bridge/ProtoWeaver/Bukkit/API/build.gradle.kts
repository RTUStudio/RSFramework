val apiVersion = property("project.server.apiVersion") as String

dependencies {
    compileOnly("org.spigotmc:spigot-api:$apiVersion-R0.1-SNAPSHOT")

    implementation(project(path = ":Bridge:ProtoWeaver:Common:API", configuration = "shadow"))
}
