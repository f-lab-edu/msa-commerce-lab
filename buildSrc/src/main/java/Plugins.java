/**
 * Centralized plugin management using ENUM
 * Provides type-safe plugin IDs and versions for Gradle builds
 * Usage: id(Plugins.SPRING_BOOT.id) version Plugins.SPRING_BOOT.version
 */
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
    
    /**
     * Constructor for plugins without version (core Gradle plugins or buildSrc plugins)
     */
    Plugins(String id) {
        this.id = id;
        this.version = null;
    }
    
    /**
     * Constructor for plugins with version
     */
    Plugins(String id, Version version) {
        this.id = id;
        this.version = version;
    }
    
    /**
     * Get plugin ID for use in build.gradle.kts
     * Usage: id(Plugins.SPRING_BOOT.id)
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get plugin version for use in build.gradle.kts
     * Usage: version Plugins.SPRING_BOOT.version
     */
    public String getVersion() {
        return version != null ? version.getVersion() : null;
    }
    
    /**
     * Returns true if this plugin has a version specified
     */
    public boolean hasVersion() {
        return version != null;
    }
    
    @Override
    public String toString() {
        return hasVersion() ? id + ":" + version.getVersion() : id;
    }
}