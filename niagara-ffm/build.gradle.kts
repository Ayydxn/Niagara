val generatorClasspath: Configuration by configurations.creating

@Suppress("UNCHECKED_CAST")
val nativeContractClassNames = rootProject.extra["nativeContractClassNames"] as List<String>

val generatedJavaDirectory = layout.buildDirectory.dir("generated/bindings/java")

dependencies {
    implementation(projects.niagaraCore)

    "generatorClasspath"(projects.niagaraCodegen)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }

    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22
}

tasks.register<JavaExec>("generateFFMNativeBindings") {
    group = "native"
    description = "Generates the FFM Java classes from the native interfaces in niagara-core."

    classpath = generatorClasspath
    mainClass = "me.ayydxn.niagara.codegen.NiagaraNativeBindingsGenerator"

    outputs.dir(generatedJavaDirectory)

    args(
        listOf(
            "--backend=ffm",
            "--java-out=${generatedJavaDirectory.get().asFile.absolutePath}"
        ) + nativeContractClassNames
    )
}
