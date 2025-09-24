val apiVersion = property("api_version") as String

dependencies {
    compileOnly("org.spigotmc:spigot-api:$apiVersion-R0.1-SNAPSHOT")

    implementation(project(path = ":Library:ProtoWeaver:Common:API", configuration = "shadow"))
}
