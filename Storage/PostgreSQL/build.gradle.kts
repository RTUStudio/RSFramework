dependencies {
    implementation(project(":Storage:Common"))

    implementation(libs.hikaricp)
    implementation(libs.postgresql)
    annotationProcessor(libs.lombok)
}
