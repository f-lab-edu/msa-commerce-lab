plugins {
    id(Plugins.JAVA_LIBRARY_PLUGIN.id)
    id(Plugins.QUERYDSL_PLUGIN.id)
}

dependencies {
    // Dependencies are now handled by JavaLibraryPlugin and QueryDslPlugin
}

// QueryDSL configuration
querydsl {
    jpa = true
    querydslSourcesDir = "src/main/generated"
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java", "src/main/generated")
        }
    }
}

tasks.withType<JavaCompile> {
    options.annotationProcessorGeneratedSourcesDirectory = file("src/main/generated")
}

tasks.clean {
    delete(file("src/main/generated"))
}
