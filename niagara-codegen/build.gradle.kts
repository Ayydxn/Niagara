dependencies {
    implementation(projects.niagaraCore)
}

tasks.register<JavaExec>("generateNativeBindings") {
    group = "native"
    description = ""

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "me.ayydxn.niagara.codegen.NiagaraNativeBindingsGenerator"
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}
