dependencies {
    implementation(project(path = ":Bridge:ProtoWeaver:Velocity:API", configuration = "shadow"))
    implementation(project(path = ":Bridge:ProtoWeaver:Common:API", configuration = "shadow"))
    implementation(project(path = ":Bridge:ProtoWeaver:Common:Core", configuration = "shadow"))
    compileOnly(libs.velocity.api.latest)

    compileOnly(libs.toml4j)
    compileOnly(libs.fastutil)
}
