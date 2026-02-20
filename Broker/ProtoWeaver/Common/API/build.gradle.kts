dependencies {
    compileOnly(libs.fory)
    compileOnly(libs.netty.buffer)
    compileOnly(libs.netty.transport)
    compileOnly(libs.netty.handler)
    compileOnly(libs.netty.codec.http)
    compileOnly(libs.netty.codec.http2)
    compileOnly(libs.bouncycastle.pkix)
    compileOnly(libs.jsr305)

    implementation(project(":Broker:Common"))
}
