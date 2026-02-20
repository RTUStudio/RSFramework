dependencies {
    implementation(project(":Storage:Common"))

    implementation(libs.sqlite)
    annotationProcessor(libs.lombok)
}
