dependencies {
    implementation(project(path = ":Bridge:Proxium:Common:API", configuration = "shadow"))
    compileOnly(project(":Configurate"))
    compileOnly(project(":Bridge:Common"))

    compileOnly(libs.fory)
    compileOnly(libs.bundles.netty)
    compileOnly(libs.bouncycastle.pkix)

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("org.mockito:mockito-core:5.15.2")
    testImplementation(project(path = ":Bridge:Proxium:Common:API", configuration = "shadow"))
    testImplementation(project(":Bridge:Common"))
    testImplementation(project(":Configurate"))
    testImplementation(libs.fory)
}

tasks.test {
    useJUnitPlatform()
}
