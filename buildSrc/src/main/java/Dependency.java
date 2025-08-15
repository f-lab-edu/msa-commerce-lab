/**
 * Centralized dependency management using ENUM
 * Provides type-safe dependency coordinates with group, artifact, version, and optional classifier
 * Spring Boot starters explicitly reference Version.SPRING_BOOT for clear version management
 */
public enum Dependency {
    // Spring Boot Starters (explicitly using Spring Boot version)
    SPRING_BOOT_STARTER("org.springframework.boot", "spring-boot-starter", Version.SPRING_BOOT),
    SPRING_BOOT_STARTER_WEB("org.springframework.boot", "spring-boot-starter-web", Version.SPRING_BOOT),
    SPRING_BOOT_STARTER_DATA_JPA("org.springframework.boot", "spring-boot-starter-data-jpa", Version.SPRING_BOOT),
    SPRING_BOOT_STARTER_DATA_REDIS("org.springframework.boot", "spring-boot-starter-data-redis", Version.SPRING_BOOT),
    SPRING_BOOT_STARTER_SECURITY("org.springframework.boot", "spring-boot-starter-security", Version.SPRING_BOOT),
    SPRING_BOOT_STARTER_VALIDATION("org.springframework.boot", "spring-boot-starter-validation", Version.SPRING_BOOT),
    SPRING_BOOT_STARTER_TEST("org.springframework.boot", "spring-boot-starter-test", Version.SPRING_BOOT),
    
    // Database
    MYSQL_CONNECTOR("mysql", "mysql-connector-java", "8.0.33"),
    H2_DATABASE("com.h2database", "h2"),  // Version managed by Spring Boot
    
    // Database Migration
    FLYWAY_CORE("org.flywaydb", "flyway-core"),  // Version managed by Spring Boot
    FLYWAY_MYSQL("org.flywaydb", "flyway-mysql"),  // Version managed by Spring Boot
    
    // QueryDSL
    QUERYDSL_JPA("com.querydsl", "querydsl-jpa", "5.0.0", "jakarta"),
    QUERYDSL_APT("com.querydsl", "querydsl-apt", "5.0.0", "jakarta"),
    JAKARTA_ANNOTATION("jakarta.annotation", "jakarta.annotation-api"),
    JAKARTA_PERSISTENCE("jakarta.persistence", "jakarta.persistence-api"),
    
    // JWT
    JJWT_API("io.jsonwebtoken", "jjwt-api", "0.11.5"),
    JJWT_IMPL("io.jsonwebtoken", "jjwt-impl", "0.11.5"),
    JJWT_JACKSON("io.jsonwebtoken", "jjwt-jackson", "0.11.5"),
    
    // Kafka
    SPRING_KAFKA("org.springframework.kafka", "spring-kafka"),  // Version managed by Spring Boot
    
    // Documentation
    SPRINGDOC_OPENAPI("org.springdoc", "springdoc-openapi-starter-webmvc-ui", "2.8.9"),
    
    // Lombok
    LOMBOK("org.projectlombok", "lombok"),  // Version managed by Spring Boot
    
    // Testing
    SPRING_SECURITY_TEST("org.springframework.security", "spring-security-test"),
    TESTCONTAINERS_JUNIT("org.testcontainers", "junit-jupiter"),  // Version managed by Spring Boot
    TESTCONTAINERS_MYSQL("org.testcontainers", "mysql");  // Version managed by Spring Boot
    
    private final String group;
    private final String artifact;
    private final String version;
    private final String classifier;
    
    /**
     * Constructor for dependencies without version (managed by Spring Boot)
     */
    Dependency(String group, String artifact) {
        this(group, artifact, (String) null, null);
    }
    
    /**
     * Constructor for dependencies with Version enum
     */
    Dependency(String group, String artifact, Version version) {
        this(group, artifact, version.getVersion(), null);
    }
    
    /**
     * Constructor for dependencies with Version enum and classifier
     */
    Dependency(String group, String artifact, Version version, String classifier) {
        this(group, artifact, version.getVersion(), classifier);
    }
    
    /**
     * Constructor for dependencies with version string
     */
    Dependency(String group, String artifact, String version) {
        this(group, artifact, version, null);
    }
    
    /**
     * Constructor for dependencies with version string and classifier
     */
    Dependency(String group, String artifact, String version, String classifier) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.classifier = classifier;
    }
    
    public String getGroup() {
        return group;
    }
    
    public String getArtifact() {
        return artifact;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getClassifier() {
        return classifier;
    }
    
    /**
     * Returns the full Maven coordinate string
     * Format: group:artifact[:version][:classifier]
     */
    public String getCoordinate() {
        StringBuilder sb = new StringBuilder();
        sb.append(group).append(":").append(artifact);
        
        if (version != null && !version.isEmpty()) {
            sb.append(":").append(version);
            
            if (classifier != null && !classifier.isEmpty()) {
                sb.append(":").append(classifier);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Returns the coordinate with Spring Boot version for Spring Boot starters
     * Format: group:artifact:springBootVersion[:classifier]
     * Note: Spring Boot starters now explicitly reference Version.SPRING_BOOT
     */
    public String getCoordinateWithSpringBootVersion() {
        // Since Spring Boot starters now explicitly use Version.SPRING_BOOT,
        // this method simply returns the normal coordinate
        return getCoordinate();
    }
    
    /**
     * Returns true if this dependency has a version specified
     */
    public boolean hasVersion() {
        return version != null && !version.isEmpty();
    }
    
    /**
     * Returns true if this dependency has a classifier specified
     */
    public boolean hasClassifier() {
        return classifier != null && !classifier.isEmpty();
    }
    
    @Override
    public String toString() {
        return getCoordinate();
    }
}