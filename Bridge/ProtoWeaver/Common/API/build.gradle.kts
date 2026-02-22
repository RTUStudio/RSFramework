dependencies {
    compileOnly(libs.fory)
    compileOnly(libs.bundles.netty)
    compileOnly(libs.bouncycastle.pkix)
    compileOnly(libs.jsr305)

    implementation(project(":Bridge:Common"))
}
