dependencies {
    implementation(project(":common"))

    // Service-specific dependencies
    implementation(BuildDependencies.getSpringKafka())

    // Validation
    implementation(BuildDependencies.getSpringBootStarterValidation())

    // Cache
    implementation(BuildDependencies.getSpringBootStarterDataRedis())

    // Monitoring
    implementation(BuildDependencies.getSpringBootStarterActuator())
    implementation(BuildDependencies.getMicrometerPrometheus())

    // MapStruct
    implementation(BuildDependencies.getMapstruct())
    annotationProcessor(BuildDependencies.getMapstructProcessor())

    // Database connector
    runtimeOnly(BuildDependencies.getMysqlConnectorRuntime())

    // Test
    testImplementation(BuildDependencies.getTestcontainersJunit())
    testImplementation(BuildDependencies.getTestcontainersMysql())
    testImplementation(BuildDependencies.getTestcontainersKafka())
    testImplementation(BuildDependencies.getSpringKafkaTest())
}
