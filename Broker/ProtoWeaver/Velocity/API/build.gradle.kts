dependencies {
    implementation(project(path = ":Broker:ProtoWeaver:Common:API", configuration = "shadow"))
    compileOnly(libs.velocity.api)

    compileOnly(libs.toml4j)
}
