plugins {
    id("ultralight-libraries")
}

object DependencyProperties {
    const val SLF4J_VERSION: String = "2.0.20"
    const val LOGBACK_VERSION: String = "1.6.8"
}

val requestedBackend = providers.gradleProperty("backend")
    .orElse("jni")
    .get()

dependencies {
    // Core
    implementation(projects.niagaraApi)

    runtimeOnly(projects.niagaraJni)
    runtimeOnly(projects.niagaraFfm)

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
