dependencies {
    implementation(projects.niagaraCore)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}
