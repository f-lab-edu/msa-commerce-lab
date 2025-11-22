public final class BuildDependencies {

    // Prevent instantiation
    private BuildDependencies() {
    }

    // Flyway buildscript dependencies
    public static String getMysqlConnector() {
        return Dependency.MYSQL_CONNECTOR.getCoordinate();
    }

    public static String getFlywayMysql() {
        return "org.flywaydb:flyway-mysql:" + Version.FLYWAY.getVersion();
    }

    // Common service dependencies
    public static String getSpringKafka() {
        return Dependency.SPRING_KAFKA.getCoordinate();
    }

    public static String getMysqlConnectorRuntime() {
        return Dependency.MYSQL_CONNECTOR.getCoordinate();
    }

    // Security dependencies - using smart versioning
    public static String getSpringBootStarterSecurity() {
        return Dependency.SPRING_BOOT_STARTER_SECURITY.getCoordinateWithSpringBootVersion();
    }

    public static String getSpringBootStarterDataRedis() {
        return Dependency.SPRING_BOOT_STARTER_DATA_REDIS.getCoordinateWithSpringBootVersion();
    }

    public static String getSpringBootStarterValidation() {
        return Dependency.SPRING_BOOT_STARTER_VALIDATION.getCoordinateWithSpringBootVersion();
    }

    public static String getSpringBootStarterActuator() {
        return Dependency.SPRING_BOOT_STARTER_ACTUATOR.getCoordinateWithSpringBootVersion();
    }

    public static String getMicrometerPrometheus() {
        return Dependency.MICROMETER_PROMETHEUS.getCoordinate();
    }

    // JWT dependencies
    public static String getJjwtApi() {
        return Dependency.JJWT_API.getCoordinate();
    }

    public static String getJjwtImpl() {
        return Dependency.JJWT_IMPL.getCoordinate();
    }

    public static String getJjwtJackson() {
        return Dependency.JJWT_JACKSON.getCoordinate();
    }

    // Testing dependencies
    public static String getSpringSecurityTest() {
        return Dependency.SPRING_SECURITY_TEST.getCoordinate();
    }

    public static String getTestcontainersJunit() {
        return Dependency.TESTCONTAINERS_JUNIT.getCoordinate();
    }

    public static String getTestcontainersMysql() {
        return Dependency.TESTCONTAINERS_MYSQL.getCoordinate();
    }

    public static String getTestcontainersKafka() {
        return Dependency.TESTCONTAINERS_KAFKA.getCoordinate();
    }

    public static String getSpringKafkaTest() {
        return Dependency.SPRING_KAFKA_TEST.getCoordinate();
    }

    // MapStruct dependencies
    public static String getMapstruct() {
        return Dependency.MAPSTRUCT.getCoordinate();
    }

    public static String getMapstructProcessor() {
        return Dependency.MAPSTRUCT_PROCESSOR.getCoordinate();
    }

    // UUID Generator
    public static String getUuidGenerator() {
        return Dependency.UUID_GENERATOR.getCoordinate();
    }

}
