dependencies {
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:Core", configuration = "shadow"))

    implementation(project(":Framework:API"))

    implementation(project(path = ":Framework:NMS:1_17_R1", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_18_R1", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_18_R2", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_19_R1", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_19_R2", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_19_R3", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_20_R1", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_20_R2", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_20_R3", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_20_R4", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_21_R1", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_21_R2", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_21_R3", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_21_R4", configuration = "reobf"))
    implementation(project(path = ":Framework:NMS:1_21_R5", configuration = "reobf"))

    implementation(project(":Platform:Spigot"))
    implementation(project(":Platform:Paper"))
    implementation(project(":Platform:Folia"))
}
