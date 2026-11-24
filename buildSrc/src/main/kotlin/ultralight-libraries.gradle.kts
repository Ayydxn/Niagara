import java.util.Properties

open class UltralightLibrariesExtension {
    lateinit var sdkLocation: String
    var libraryDirectory: String? = null
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists())
        localPropertiesFile.inputStream().use { load(it) }
}

val resolvedSdkLocation: String = localProperties.getProperty("ultralight_sdk_location")
    ?: findProperty("ultralight_sdk_location") as String?
    ?: ""

val resolvedLibraryDirectory: File? = if (resolvedSdkLocation.isBlank()) {
    null
} else {
    listOf(File(resolvedSdkLocation, "bin")).firstOrNull { directory ->
        directory.isDirectory && directory.listFiles().orEmpty().any {
            it.name.contains("UltralightCore") && it.extension in setOf("dll", "so", "dylib")
        }
    }
}

if (resolvedSdkLocation.isNotBlank() && resolvedLibraryDirectory == null)
    logger.warn("[${project.name}] No Ultralight libraries were found in $resolvedSdkLocation, so anything needing them will be skipped.")

val ultralightLibraries = extensions.create<UltralightLibrariesExtension>("ultralightLibraries").apply {
    sdkLocation = resolvedSdkLocation
    libraryDirectory = resolvedLibraryDirectory?.absolutePath
}

tasks.withType<Test>().configureEach {
    if (resolvedLibraryDirectory != null)
        systemProperty("niagara.libraries.path", resolvedLibraryDirectory.absolutePath)
}
