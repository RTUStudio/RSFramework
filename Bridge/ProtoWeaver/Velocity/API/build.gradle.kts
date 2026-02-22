dependencies {
    implementation(project(path = ":Bridge:ProtoWeaver:Common:API", configuration = "shadow"))
    compileOnly(libs.velocity.api)

    compileOnly(libs.toml4j)
}
