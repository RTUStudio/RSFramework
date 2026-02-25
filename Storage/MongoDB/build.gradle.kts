dependencies {
    implementation(project(":Storage:Common"))

    compileOnly(libs.mongodb.sync)
    compileOnly(libs.mongodb.bson)
}
