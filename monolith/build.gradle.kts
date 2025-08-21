dependencies {
    implementation(project(":common"))

    // Service-specific dependencies
    implementation(BuildDependencies.getSpringBootStarterSecurity())
    implementation(BuildDependencies.getSpringBootStarterDataRedis())
    implementation(BuildDependencies.getSpringKafka())

    // JWT
    implementation(BuildDependencies.getJjwtApi())
    runtimeOnly(BuildDependencies.getJjwtImpl())
    runtimeOnly(BuildDependencies.getJjwtJackson())

    // MapStruct
    implementation(BuildDependencies.getMapstruct())
    annotationProcessor(BuildDependencies.getMapstructProcessor())

    // Database connector
    runtimeOnly(BuildDependencies.getMysqlConnectorRuntime())

    // Test
    testImplementation(BuildDependencies.getSpringSecurityTest())
    testImplementation(BuildDependencies.getTestcontainersJunit())
    testImplementation(BuildDependencies.getTestcontainersMysql())
}
