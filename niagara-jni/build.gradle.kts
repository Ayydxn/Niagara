import java.security.MessageDigest

plugins {
    id("ultralight-libraries")
}

val generatorClasspath = configurations.create("generatorClasspath")

@Suppress("UNCHECKED_CAST")
val nativeContractClassNames = rootProject.extra["nativeContractClassNames"] as List<String>

val generatedJavaDirectory = layout.buildDirectory.dir("generated/bindings/java")
val generatedNativeDirectory = layout.buildDirectory.dir("generated/bindings/native")

val ultralightSdkLocation = ultralightLibraries.sdkLocation
if (ultralightSdkLocation.isBlank()) {
    logger.warn(
        "\n[niagara-jni] WARNING: 'ultralight_sdk_location' is not set. JNI bridge library compilation will be skipped.\n" +
            "  Set it in local.properties or pass -Pultralight_sdk_location=<path>.\n"
    )
}

// When set, the native build is skipped, and this directory (laid out as <platform>/<library>) is bundled as-is instead.
// CI uses this, so the final jar contains the exact JNI bridge binaries that were built and tested.
val prebuiltNativesLocation: String = (findProperty("niagara_prebuilt_natives") as String?) ?: ""
val usePrebuiltNatives = prebuiltNativesLocation.isNotBlank()

val osName: String = System.getProperty("os.name").lowercase()
val osArchitecture: String = System.getProperty("os.arch").lowercase()

val isWindows = osName.startsWith("windows")
val isMacOS = osName.contains("mac")
val isLinux = osName.contains("linux")

val isX64 = osArchitecture == "amd64" || osArchitecture == "x86_64"
val isArm64 = osArchitecture == "aarch64" || osArchitecture == "arm64"

// These platform ids and library file names must stay in sync with NativeLibraryPlatform in niagara-core.
val platform: String = (findProperty("platform") as String?) ?: when {
    isWindows && isX64 -> "windows-x64"
    isMacOS && isArm64 -> "macos-arm64"
    isLinux && isX64 -> "linux-x64"
    else -> throw GradleException("Unsupported host platform: $osName ($osArchitecture). Supported platforms are windows-x64, macos-arm64 and linux-x64. Use -Pplatform=<id> to override.")
}

val nativeLibraryName = when (platform) {
    "windows-x64" -> "NiagaraJNIBridge.dll"
    "macos-arm64" -> "libNiagaraJNIBridge.dylib"
    else -> "libNiagaraJNIBridge.so"
}

val nativeChecksumDirectory = layout.buildDirectory.dir("native-checksums/$platform").get().asFile
val cmakeBuildDirectory = layout.buildDirectory.dir("cmake-build").get().asFile

// Multi-config generators like Visual Studio put the library in a per-configuration folder, single-config generators such as Makefiles don't.
fun findBuiltNativeLibrary(): File? = listOf(
    File(cmakeBuildDirectory, "Release/$nativeLibraryName"),
    File(cmakeBuildDirectory, nativeLibraryName)
).firstOrNull { it.exists() }

sourceSets {
    main {
        java.srcDir(generatedJavaDirectory)
    }
}

dependencies {
    implementation(projects.niagaraCore)

    "generatorClasspath"(projects.niagaraCodegen)

    testImplementation(projects.niagaraApi)
    testImplementation(testFixtures(projects.niagaraCore))
}

val configureCMake = tasks.register("configureCMake", Exec::class) {
    group = "native"
    description = "Runs CMake to configure the project files for our native build"

    dependsOn(generateJNINativeBindings)

    onlyIf { !usePrebuiltNatives && ultralightSdkLocation.isNotBlank() }

    doFirst { cmakeBuildDirectory.mkdirs() }

    workingDir = cmakeBuildDirectory

    commandLine(
        "cmake",
        file("src/main/native").absolutePath,
        "-DULTRALIGHT_SDK=${ultralightSdkLocation}",
        "-DNIAGARA_GENERATED_DIR=${generatedNativeDirectory.get().asFile.absolutePath}",
        "-DCMAKE_BUILD_TYPE=Release",
        "-G", if (isWindows) "Visual Studio 18 2026" else "Unix Makefiles"
    )
}

val compileNative = tasks.register("compileNative", Exec::class) {
    group = "native"
    description = "Compiles the native C++ code via CMake"

    dependsOn(configureCMake)

    onlyIf { !usePrebuiltNatives && ultralightSdkLocation.isNotBlank() }

    workingDir = cmakeBuildDirectory
    commandLine("cmake", "--build", ".", "--config", "Release", "--parallel")

    doLast {
        val produced = findBuiltNativeLibrary()
            ?: throw GradleException("CMake finished, but $nativeLibraryName was not found in $cmakeBuildDirectory (checked Release/ and the build root).")

        val checksum = MessageDigest.getInstance("SHA-256")
            .digest(produced.readBytes())
            .joinToString("") { "%02x".format(it) }

        // Written outside the bundled resources so it never ships inside the jar.
        nativeChecksumDirectory.mkdirs()

        val checksumFile = File(nativeChecksumDirectory, "$nativeLibraryName.sha256")
        checksumFile.writeText(checksum)

        logger.quiet("[niagara-jni] Wrote native library checksum to ${checksumFile.absolutePath}")
    }
}

val stageJNIBridgeLibrary = tasks.register<Sync>("stageJNIBridgeLibrary") {
    group = "native"
    description = "Stages the compiled JNI bridge library so it's bundled into the jar's resources"

    if (usePrebuiltNatives) {
        val prebuiltNativesDirectory = rootProject.file(prebuiltNativesLocation)

        doFirst {
            if (!prebuiltNativesDirectory.isDirectory)
                throw GradleException("niagara_prebuilt_natives points to $prebuiltNativesDirectory which isn't a directory.")
        }

        from(prebuiltNativesDirectory) {
            into("natives")
        }
    } else {
        dependsOn(compileNative)

        onlyIf { ultralightSdkLocation.isNotBlank() }

        from(cmakeBuildDirectory) {
            include("Release/$nativeLibraryName", nativeLibraryName)
            eachFile { relativePath = RelativePath(true, "natives", platform, name) }

            includeEmptyDirs = false
        }
    }

    into(layout.buildDirectory.dir("generated/niagara-natives"))
}

val generateJNINativeBindings = tasks.register<JavaExec>("generateJNINativeBindings") {
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

tasks.processResources {
    from(stageJNIBridgeLibrary)
}

listOf("compileJava", "sourcesJar", "javadoc").forEach { taskName ->
    tasks.named(taskName) { dependsOn(generateJNINativeBindings) }
}
