dependencies {
    implementation(project(":common"))

    // Service-specific dependencies
    implementation(BuildDependencies.getSpringKafka())

    // Validation
    implementation(BuildDependencies.getSpringBootStarterValidation())

    // AOP (common 모듈의 @ValidateCommand 애스펙트 구동에 필요)
    implementation(BuildDependencies.getSpringBootStarterAop())

    // Monitoring
    implementation(BuildDependencies.getSpringBootStarterActuator())
    implementation(BuildDependencies.getMicrometerPrometheus())

    // Database connector
    runtimeOnly(BuildDependencies.getMysqlConnectorRuntime())

    // Test
    testImplementation(BuildDependencies.getTestcontainersJunit())
    testImplementation(BuildDependencies.getTestcontainersMysql())
}
