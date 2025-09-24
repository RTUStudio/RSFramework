subprojects {
    apply(plugin = "io.papermc.paperweight.userdev")

    dependencies {
        compileOnly(project(":Framework:API"))
        add("pluginRemapper", "net.fabricmc:tiny-remapper:0.10.4:fat")
    }
}
