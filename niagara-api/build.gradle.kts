sourceSets {
    test {
        compileClasspath += sourceSets.main.get().compileClasspath
    }
}

dependencies {
    api(projects.niagaraCore)
}
