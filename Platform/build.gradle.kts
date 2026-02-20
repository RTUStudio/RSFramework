val catalog = the<VersionCatalogsExtension>().named("libs")

subprojects {
    dependencies {
        compileOnly(project(":Broker:ProtoWeaver:Common:API"))
        implementation(catalog.findLibrary("configurate-yaml").get())
    }
}
