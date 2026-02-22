subprojects {
    tasks.shadowJar {
        relocate("org.apache.fory", "kr.rtustudio.bridge.fory")
    }
}
