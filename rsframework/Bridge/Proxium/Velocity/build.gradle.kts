dependencies {
    implementation(project(path = ":Bridge:Proxium:Common:API", configuration = "shadow"))
    implementation(project(path = ":Bridge:Proxium:Common:Core", configuration = "shadow"))
    compileOnly(project(":Configurate"))
    compileOnly(libs.velocity.api.latest)

    compileOnly(libs.toml4j)
    compileOnly(libs.fastutil)
}
