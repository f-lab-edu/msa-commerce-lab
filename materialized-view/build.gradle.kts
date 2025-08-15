dependencies {
    implementation(project(":common"))

    // Service-specific dependencies
    implementation(BuildDependencies.getSpringBootStarterDataRedis())
    implementation(BuildDependencies.getSpringKafka())

    // Database Migration
    implementation(BuildDependencies.getFlywayCore())
    implementation(BuildDependencies.getFlywayMysqlDep())
    runtimeOnly(BuildDependencies.getMysqlConnectorRuntime())

    // Test
    testImplementation(BuildDependencies.getTestcontainersJunit())
    testImplementation(BuildDependencies.getTestcontainersMysql())
}
