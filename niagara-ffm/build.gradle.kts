dependencies {
    implementation(projects.niagaraCore)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }

    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22
}

