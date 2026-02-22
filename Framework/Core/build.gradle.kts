dependencies {

    implementation(project(path = ":Bridge:ProtoWeaver:Bukkit:Core", configuration = "shadow"))
    implementation(project(":Bridge:Redisson"))

    implementation(project(":Framework:API"))

    implementation(project(":Storage:Common"))
    implementation(project(":Storage:MySQL"))
    implementation(project(":Storage:MariaDB"))
    implementation(project(":Storage:MongoDB"))
    implementation(project(":Storage:Json"))
    implementation(project(":Storage:SQLite"))
    implementation(project(":Storage:PostgreSQL"))

    implementation(project(path = ":Framework:NMS:1_20_R1", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_20_R2", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_20_R3", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_20_R4", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_21_R1", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_21_R2", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_21_R3", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_21_R4", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_21_R5", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_21_R6", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_21_R7", configuration = "reobf"))

    implementation(project(":Platform:Spigot"))
    implementation(project(":Platform:Paper"))
    implementation(project(":Platform:Folia"))

    implementation(libs.libby.bukkit)
}
