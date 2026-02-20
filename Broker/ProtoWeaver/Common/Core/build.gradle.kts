dependencies {
    implementation(project(path = ":Broker:ProtoWeaver:Common:API", configuration = "shadow"))

    compileOnly(libs.fory)
    compileOnly(libs.netty.buffer)
    compileOnly(libs.netty.transport)
    compileOnly(libs.netty.handler)
    compileOnly(libs.netty.codec.http)
    compileOnly(libs.netty.codec.http2)
    compileOnly(libs.bouncycastle.pkix)
}
