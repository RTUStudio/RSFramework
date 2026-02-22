dependencies {
    implementation(project(":LightDI"))
    implementation(project(path = ":Bridge:ProtoWeaver:Bukkit:API", configuration = "shadow"))

    compileOnly(project(":Framework:API"))

    implementation(project(":Platform:Spigot"))
    implementation(project(":Platform:Paper"))

    compileOnly("dev.folia:folia-api:1.19.4-R0.1-SNAPSHOT")
}
