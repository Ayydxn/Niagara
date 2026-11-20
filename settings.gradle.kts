plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Niagara"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "niagara-api",
    "niagara-codegen",
    "niagara-core",
    "niagara-jni",
    "niagara-ffm"
)
