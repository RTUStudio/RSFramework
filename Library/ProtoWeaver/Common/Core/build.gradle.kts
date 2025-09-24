dependencies {
    implementation(project(path = ":Library:ProtoWeaver:Common:API", configuration = "shadow"))

    compileOnly("org.apache.fury:fury-core:0.10.3")
    compileOnly("io.netty:netty-buffer:4.1.97.Final")
    compileOnly("io.netty:netty-transport:4.1.97.Final")
    compileOnly("io.netty:netty-handler:4.1.97.Final")
    compileOnly("io.netty:netty-codec-http:4.1.97.Final")
    compileOnly("io.netty:netty-codec-http2:4.1.97.Final")
    compileOnly("org.bouncycastle:bcpkix-jdk18on:1.80")
}
