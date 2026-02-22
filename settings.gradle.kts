plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "RSFramework"

include("LightDI")

include(
    "Storage",
    "Storage:Common",
    "Storage:MySQL",
    "Storage:MariaDB",
    "Storage:MongoDB",
    "Storage:Json",
    "Storage:SQLite",
    "Storage:PostgreSQL",
)

include(
    "Bridge",
    "Bridge:Common",
    "Bridge:Redisson",
)

include(
    "Bridge:ProtoWeaver:Common:API",
    "Bridge:ProtoWeaver:Common:Core",
)

include(
    "Bridge:ProtoWeaver:Bukkit:API",
    "Bridge:ProtoWeaver:Bukkit:Core",
)

include(
    "Bridge:ProtoWeaver:Bungee:API",
    "Bridge:ProtoWeaver:Bungee:Core",
)

include(
    "Bridge:ProtoWeaver:Velocity:API",
    "Bridge:ProtoWeaver:Velocity:Core",
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
    "Framework:NMS:1_21_R6",
    "Framework:NMS:1_21_R7",
)

include(
    "Platform:Velocity",
    "Platform:Bungee",
)
