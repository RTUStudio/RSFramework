dependencies {
    implementation(project(":Configurate"))

    compileOnly(libs.fory)
    compileOnly(libs.bundles.netty)
    compileOnly(libs.bouncycastle.pkix)
    compileOnly(libs.jsr305)

    implementation(project(":Bridge:Common"))
}

tasks.shadowJar {
    exclude("kr/rtustudio/configure/**")
    exclude("org/spongepowered/configurate/**")
    exclude("org/yaml/snakeyaml/**")
    exclude("io/leangen/geantyref/**")
}
