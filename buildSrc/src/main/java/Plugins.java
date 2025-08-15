public enum Plugins {
    // Core Java plugins (no version needed)
    JAVA("java"),
    JAVA_LIBRARY("java-library"),
    JACOCO("jacoco"),

    // Spring Boot ecosystem
    SPRING_BOOT("org.springframework.boot", Version.SPRING_BOOT),
    SPRING_DEPENDENCY_MANAGEMENT("io.spring.dependency-management", Version.DEPENDENCY_MANAGEMENT),

    // Database & Migration
    FLYWAY("org.flywaydb.flyway", Version.FLYWAY),

    // Code Quality & Analysis
    SONARQUBE("org.sonarqube", Version.SONARQUBE),

    // QueryDSL
    QUERYDSL("com.ewerk.gradle.plugins.querydsl", Version.QUERYDSL_PLUGIN),

    // Custom buildSrc plugins (no version needed)
    JAVA_LIBRARY_PLUGIN("java-library-plugin"),
    QUERYDSL_PLUGIN("querydsl-plugin");

    private final String id;

    private final Version version;

    Plugins(String id) {
        this.id = id;
        this.version = null;
    }

    Plugins(String id, Version version) {
        this.id = id;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version != null ? version.getVersion() : null;
    }

    public boolean hasVersion() {
        return version != null;
    }

    @Override
    public String toString() {
        return hasVersion() ? id + ":" + version.getVersion() : id;
    }
}
