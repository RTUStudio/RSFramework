dependencies {
    implementation(project(":Storage:Common"))

    implementation(libs.hikaricp)
    implementation(libs.mysql)
    annotationProcessor(libs.lombok)
}
