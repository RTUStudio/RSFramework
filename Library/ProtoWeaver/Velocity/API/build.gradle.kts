dependencies {
    implementation(project(path = ":Library:ProtoWeaver:Common:API", configuration = "shadow"))
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    compileOnly("com.moandjiezana.toml:toml4j:0.7.2")
}
