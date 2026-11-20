tasks.register<JavaExec>("generateNativeBindings") {
    group = "native"
    description = ""

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "me.ayydxn.niagara.codegen.NiagaraNativeBindingsGenerator"
}
