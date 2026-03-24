dependencies {
    implementation(project(":Bridge:Common"))

    compileOnly(libs.redisson)
    compileOnly(libs.fastutil)
}
