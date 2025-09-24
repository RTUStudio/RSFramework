plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "RSFramework"

include("Library:LightDI")

include(
    "Library:ProtoWeaver:Common:API",
    "Library:ProtoWeaver:Common:Core",
)

include(
    "Library:ProtoWeaver:Bukkit:API",
    "Library:ProtoWeaver:Bukkit:Core",
    "Library:ProtoWeaver:Bukkit:NMS",
)

include(
    "Library:ProtoWeaver:Bungee:API",
    "Library:ProtoWeaver:Bungee:Core",
)

include(
    "Library:ProtoWeaver:Velocity:API",
    "Library:ProtoWeaver:Velocity:Core",
)

include(
    "Library:ProtoWeaver:Bukkit:NMS:1_20_R1",
    "Library:ProtoWeaver:Bukkit:NMS:1_20_R2",
    "Library:ProtoWeaver:Bukkit:NMS:1_20_R3",
    "Library:ProtoWeaver:Bukkit:NMS:1_20_R4",
    "Library:ProtoWeaver:Bukkit:NMS:1_21_R1",
    "Library:ProtoWeaver:Bukkit:NMS:1_21_R2",
    "Library:ProtoWeaver:Bukkit:NMS:1_21_R3",
    "Library:ProtoWeaver:Bukkit:NMS:1_21_R4",
    "Library:ProtoWeaver:Bukkit:NMS:1_21_R5",
)

include("Platform")

include(
    "Framework",
    "Framework:API",
    "Framework:Core",
    "Framework:NMS",
)

include(
    "Platform:Spigot",
    "Platform:Paper",
    "Platform:Folia",
)

include(
    "Framework:NMS:1_20_R1",
    "Framework:NMS:1_20_R2",
    "Framework:NMS:1_20_R3",
    "Framework:NMS:1_20_R4",
    "Framework:NMS:1_21_R1",
    "Framework:NMS:1_21_R2",
    "Framework:NMS:1_21_R3",
    "Framework:NMS:1_21_R4",
    "Framework:NMS:1_21_R5",
)

include(
    "Platform:Velocity",
    "Platform:Bungee",
)
