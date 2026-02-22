dependencies {
    implementation(project(":Storage:Common"))

    compileOnly(libs.hikaricp)
    compileOnly(libs.postgresql)
    annotationProcessor(libs.lombok)
}
