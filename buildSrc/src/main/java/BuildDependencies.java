/**
 * Utility class to provide easy access to dependencies in build.gradle.kts files
 */
public final class BuildDependencies {
    
    // Prevent instantiation
    private BuildDependencies() {}
    
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
    
    public static String getFlywayCore() {
        return Dependency.FLYWAY_CORE.getCoordinate();
    }
    
    public static String getFlywayMysqlDep() {
        return Dependency.FLYWAY_MYSQL.getCoordinate();
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
    
    // Plugin versions - for use in plugins block
    public static String getSpringBootVersion() {
        return Version.SPRING_BOOT.getVersion();
    }
    
    public static String getDependencyManagementVersion() {
        return Version.DEPENDENCY_MANAGEMENT.getVersion();
    }
    
    public static String getSonarqubeVersion() {
        return Version.SONARQUBE.getVersion();
    }
    
    public static String getFlywayVersion() {
        return Version.FLYWAY.getVersion();
    }
    
    
    // Additional Spring Boot starters with smart versioning
    public static String getSpringBootStarter() {
        return Dependency.SPRING_BOOT_STARTER.getCoordinateWithSpringBootVersion();
    }
    
    public static String getSpringBootStarterWeb() {
        return Dependency.SPRING_BOOT_STARTER_WEB.getCoordinateWithSpringBootVersion();
    }
    
    public static String getSpringBootStarterDataJpa() {
        return Dependency.SPRING_BOOT_STARTER_DATA_JPA.getCoordinateWithSpringBootVersion();
    }
    
    public static String getSpringBootStarterValidation() {
        return Dependency.SPRING_BOOT_STARTER_VALIDATION.getCoordinateWithSpringBootVersion();
    }
    
    public static String getSpringBootStarterTest() {
        return Dependency.SPRING_BOOT_STARTER_TEST.getCoordinateWithSpringBootVersion();
    }
}