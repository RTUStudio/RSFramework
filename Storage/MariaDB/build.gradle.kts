dependencies {
    implementation(project(":Storage:Common"))

    implementation(libs.hikaricp)
    implementation(libs.mariadb)
    annotationProcessor(libs.lombok)
}
