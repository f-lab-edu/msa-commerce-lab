dependencies {
    implementation(project(":common"))

    // Service-specific dependencies
    // Redis dependency moved to common module
    implementation(BuildDependencies.getSpringKafka())

    // Database connector
    runtimeOnly(BuildDependencies.getMysqlConnectorRuntime())

    // Test
    testImplementation(BuildDependencies.getTestcontainersJunit())
    testImplementation(BuildDependencies.getTestcontainersMysql())
}
