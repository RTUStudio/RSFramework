subprojects {
    dependencies {
        compileOnly(project(":Library:ProtoWeaver:Common:API"))
        implementation("org.spongepowered:configurate-yaml:4.2.0-GeyserMC-SNAPSHOT")
    }
}
