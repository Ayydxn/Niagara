plugins {
    `java-test-fixtures`
}

dependencies {
    testFixturesCompileOnly("org.jetbrains:annotations:${rootProject.property("jetbrains_annotations_version")}")

    testFixturesApi(platform("org.junit:junit-bom:${rootProject.property("junit_version")}"))
    testFixturesApi("org.junit.jupiter:junit-jupiter")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}
