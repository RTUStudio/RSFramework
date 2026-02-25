dependencies {
    implementation(project(path = ":Bridge:Proxium:Common:API", configuration = "shadow"))
    implementation(project(path = ":Bridge:Proxium:Common:Core", configuration = "shadow"))
    compileOnly(project(":Configurate"))
    compileOnly("io.github.waterfallmc:waterfall-api:1.20-R0.3-SNAPSHOT")
    compileOnly(libs.fastutil)
}
