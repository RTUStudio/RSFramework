subprojects {
    dependencies {
        compileOnly(project(":Bridge:Proxium:Common:API"))
        compileOnly(project(":Configurate"))
    }
}
