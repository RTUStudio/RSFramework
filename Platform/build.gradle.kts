val catalog = the<VersionCatalogsExtension>().named("libs")

subprojects {
    dependencies {
        compileOnly(project(":Bridge:ProtoWeaver:Common:API"))
        implementation(catalog.findLibrary("configurate-yaml").get())
    }
}
