dependencies {
    implementation(project(":Storage:Common"))

    implementation(libs.mongodb.sync)
    implementation(libs.mongodb.bson)
    annotationProcessor(libs.lombok)
}
