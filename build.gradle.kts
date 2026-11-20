plugins {
    id("java-library")
}

base.archivesName.set(rootProject.extra["archives_base_name"].toString())

allprojects {
    group = rootProject.property("maven_group").toString()
    version = rootProject.property("version").toString()

    repositories {
        mavenCentral()
    }
}

subprojects {
    pluginManager.apply("java-library")

    dependencies {
        testImplementation(platform("org.junit:junit-bom:${rootProject.property("junit_version")}"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }

        withJavadocJar()
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc> {
        val options = options as StandardJavadocDocletOptions
        options.addStringOption("Xdoclint:all,-missing", "-quiet")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        failOnNoDiscoveredTests = false
    }
}
