subprojects {
    tasks.shadowJar {
        relocate("org.apache.fury", "kr.rtustudio.protoweaver.fury")
    }
}
