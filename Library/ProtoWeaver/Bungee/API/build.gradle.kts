dependencies {
    implementation(project(path = ":Library:ProtoWeaver:Common:API", configuration = "shadow"))
    compileOnly("io.github.waterfallmc:waterfall-api:1.20-R0.3-SNAPSHOT")
}
