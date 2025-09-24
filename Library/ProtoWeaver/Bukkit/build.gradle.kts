val paper_plugin: String by project
val api_version: String by project

subprojects {
    dependencies {
        if (paper_plugin.toBoolean()) {
            compileOnly("io.papermc.paper:paper-api:${api_version}-R0.1-SNAPSHOT")
        } else {
            compileOnly("org.spigotmc:spigot-api:${api_version}-R0.1-SNAPSHOT")
        }
    }
}
