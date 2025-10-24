dependencies {
    implementation(project(":common"))

    // Service-specific dependencies
    implementation(BuildDependencies.getSpringKafka())

    // Cache
    implementation(BuildDependencies.getSpringBootStarterDataRedis())

    // MapStruct
    implementation(BuildDependencies.getMapstruct())
    annotationProcessor(BuildDependencies.getMapstructProcessor())

    // Database connector
    runtimeOnly(BuildDependencies.getMysqlConnectorRuntime())

    // Test
    testImplementation(BuildDependencies.getTestcontainersJunit())
    testImplementation(BuildDependencies.getTestcontainersMysql())
}
