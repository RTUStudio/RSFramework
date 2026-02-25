dependencies {
    implementation(project(":Storage:Common"))

    compileOnly(libs.hikaricp)
    compileOnly(libs.mysql)
}
