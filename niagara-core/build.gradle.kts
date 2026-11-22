tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}
