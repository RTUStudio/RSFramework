dependencies {
    implementation(project(":Storage:Common"))

    compileOnly(libs.sqlite)
    annotationProcessor(libs.lombok)
}
