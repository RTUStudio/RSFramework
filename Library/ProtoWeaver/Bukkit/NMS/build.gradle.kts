subprojects {
    apply(plugin = "io.papermc.paperweight.userdev")

    dependencies {
        compileOnly(project(":Library:ProtoWeaver:Bukkit:API"))
        compileOnly(project(":Library:ProtoWeaver:Common:API"))
        compileOnly(project(":Library:ProtoWeaver:Common:Core"))

        add("pluginRemapper", "net.fabricmc:tiny-remapper:0.10.4:fat")
    }

    tasks.named("reobfJar") {
        dependsOn(tasks.jar)
    }
}
