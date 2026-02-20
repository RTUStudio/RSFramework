dependencies {
    implementation(project(path = ":Broker:ProtoWeaver:Bungee:API", configuration = "shadow"))
    implementation(project(path = ":Broker:ProtoWeaver:Common:API", configuration = "shadow"))
    implementation(project(path = ":Broker:ProtoWeaver:Common:Core", configuration = "shadow"))
    compileOnly("io.github.waterfallmc:waterfall-api:1.20-R0.3-SNAPSHOT")
    implementation(libs.fastutil)
}
