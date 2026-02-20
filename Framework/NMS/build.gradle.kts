val catalog = the<VersionCatalogsExtension>().named("libs")

subprojects {
    apply(plugin = "io.papermc.paperweight.userdev")

    dependencies {
        compileOnly(project(":Framework:API"))
        add(
            "pluginRemapper",
            catalog
                .findLibrary("tiny-remapper")
                .get()
                .get()
                .toString() + ":fat",
        )
    }
}
