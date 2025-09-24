dependencies {
    implementation(project(path = ":Library:ProtoWeaver:Velocity:API", configuration = "shadow"))
    implementation(project(path = ":Library:ProtoWeaver:Common:API", configuration = "shadow"))
    implementation(project(path = ":Library:ProtoWeaver:Common:Core", configuration = "shadow"))
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    compileOnly("com.moandjiezana.toml:toml4j:0.7.2")
}
