dependencies {
    implementation(project(path = ":Bridge:ProtoWeaver:Common:API", configuration = "shadow"))

    compileOnly(libs.fory)
    compileOnly(libs.bundles.netty)
    compileOnly(libs.bouncycastle.pkix)
}
