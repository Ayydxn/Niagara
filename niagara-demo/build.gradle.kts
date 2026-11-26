plugins {
    id("ultralight-libraries")
}

object DependencyProperties {
    const val SLF4J_VERSION: String = "2.0.20"
    const val LOGBACK_VERSION: String = "1.6.8"
    const val LWJGL_VERSION: String = "3.4.3"
}

val requestedBackend = providers.gradleProperty("backend")
    .orElse("jni")
    .get()

val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "SunOS", "Unix").any { name.startsWith(it) } ->
            "natives-linux"
        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) }     ->
            "natives-macos-arm64"
        arrayOf("Windows").any { name.startsWith(it) }                ->
            "natives-windows"
        else                                                                            ->
            throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

dependencies {
    // Core
    implementation(projects.niagaraApi)

    runtimeOnly(projects.niagaraJni)
    runtimeOnly(projects.niagaraFfm)

    implementation(platform("org.lwjgl:lwjgl-bom:${DependencyProperties.LWJGL_VERSION}"))

    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-sdl")
    implementation("org.lwjgl:lwjgl::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-opengl::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-sdl::$lwjglNatives")

    // Utilities
    implementation("org.slf4j:slf4j-api:${DependencyProperties.SLF4J_VERSION}")
    implementation("ch.qos.logback:logback-classic:${DependencyProperties.LOGBACK_VERSION}")
    implementation("ch.qos.logback:logback-core:${DependencyProperties.LOGBACK_VERSION}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }

    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22
}

tasks.register("runDemoApp", JavaExec::class) {
    group = "verification"
    description = "Runs Niagara's demo application"

    mainClass = "me.ayydxn.niagara.demo.NiagaraDemo"
    classpath = sourceSets.main.get().runtimeClasspath

    if (ultralightLibraries.libraryDirectory != null)
        systemProperty("niagara.libraries.path", ultralightLibraries.libraryDirectory)

    systemProperty("niagara.nativeBackend", requestedBackend)

    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
