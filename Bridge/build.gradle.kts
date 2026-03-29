subprojects {
    tasks.shadowJar {
        relocate("org.apache.fory", "kr.rtustudio.bridge.fory")
        relocate("org.redisson", "kr.rtustudio.bridge.redisson")
    }
}
