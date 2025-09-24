dependencies {
    implementation("org.reflections:reflections:0.10.2")
}

tasks.shadowJar {
    relocate("org.reflections", "kr.rtustudio.cdi.reflections")
}
