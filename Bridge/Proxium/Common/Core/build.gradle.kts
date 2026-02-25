dependencies {
    implementation(project(path = ":Bridge:Proxium:Common:API", configuration = "shadow"))
    compileOnly(project(":Configurate"))
    compileOnly(project(":Bridge:Common"))

    compileOnly(libs.fory)
    compileOnly(libs.bundles.netty)
    compileOnly(libs.bouncycastle.pkix)
}
