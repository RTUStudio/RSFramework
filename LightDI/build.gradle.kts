dependencies {
    implementation(libs.reflections)
}

tasks.shadowJar {
    relocate("org.reflections", "kr.rtustudio.cdi.reflections")
}
