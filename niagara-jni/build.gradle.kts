val generatorClasspath: Configuration by configurations.creating

@Suppress("UNCHECKED_CAST")
val nativeContractClassNames = rootProject.extra["nativeContractClassNames"] as List<String>

val generatedJavaDirectory = layout.buildDirectory.dir("generated/bindings/java")
val generatedNativeDirectory = layout.buildDirectory.dir("generated/bindings/native")

dependencies {
    implementation(projects.niagaraCore)

    "generatorClasspath"(projects.niagaraCodegen)
}

tasks.register<JavaExec>("generateJNINativeBindings") {
    group = "native"
    description = "Generates the JNI Java classes and C++ glue from the native interfaces in niagara-core."

    classpath = generatorClasspath
    mainClass = "me.ayydxn.niagara.codegen.NiagaraNativeBindingsGenerator"

    outputs.dir(generatedJavaDirectory)
    outputs.dir(generatedNativeDirectory)

    args(
        listOf(
            "--backend=jni",
            "--java-out=${generatedJavaDirectory.get().asFile.absolutePath}",
            "--cpp-out=${generatedNativeDirectory.get().asFile.absolutePath}"
        ) + nativeContractClassNames
    )
}