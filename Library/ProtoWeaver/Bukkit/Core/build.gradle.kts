val apiVersion = property("api_version") as String

dependencies {
    compileOnly("org.spigotmc:spigot-api:$apiVersion-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:$apiVersion-R0.1-SNAPSHOT")

    implementation(project(path = ":Library:ProtoWeaver:Common:API", configuration = "shadow"))
    implementation(project(path = ":Library:ProtoWeaver:Common:Core", configuration = "shadow"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:API", configuration = "shadow"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:NMS", configuration = "shadow"))

    implementation(project(path = ":Library:ProtoWeaver:Bukkit:NMS:1_20_R1", configuration = "reobf"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:NMS:1_20_R2", configuration = "reobf"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:NMS:1_20_R3", configuration = "reobf"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:NMS:1_20_R4", configuration = "reobf"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:NMS:1_21_R1", configuration = "reobf"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:NMS:1_21_R2", configuration = "reobf"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:NMS:1_21_R3", configuration = "reobf"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:NMS:1_21_R4", configuration = "reobf"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:NMS:1_21_R5", configuration = "reobf"))
    implementation(project(path = ":Library:ProtoWeaver:Bukkit:NMS:1_21_R6", configuration = "reobf"))
}
