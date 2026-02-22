dependencies {
    implementation(project(":Bridge:Common"))

    implementation(libs.redisson)
    compileOnly(libs.fastutil)
}
