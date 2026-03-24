plugins {
    `java-library`
}

dependencies {
    api(libs.configurate.yaml)

    compileOnly(libs.fastutil)
}
