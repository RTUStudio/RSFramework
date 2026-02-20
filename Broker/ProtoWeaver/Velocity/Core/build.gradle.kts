dependencies {
    implementation(project(path = ":Broker:ProtoWeaver:Velocity:API", configuration = "shadow"))
    implementation(project(path = ":Broker:ProtoWeaver:Common:API", configuration = "shadow"))
    implementation(project(path = ":Broker:ProtoWeaver:Common:Core", configuration = "shadow"))
    compileOnly(libs.velocity.api.latest)

    compileOnly(libs.toml4j)
    implementation(libs.fastutil)
}
