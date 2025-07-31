plugins {
    `java-library`
    id("com.ewerk.gradle.plugins.querydsl") version "1.0.10"
}

dependencies {
    // Spring Boot Starters
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-validation")

    // Database
    api("mysql:mysql-connector-java:8.0.33")
    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-mysql")

    // QueryDSL
    api("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // Test
    testImplementation("com.h2database:h2")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
}

// QueryDSL 설정
querydsl {
    jpa = true
    querydslSourcesDir = "src/main/generated"
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java", "src/main/generated")
        }
    }
}

tasks.withType<JavaCompile> {
    options.annotationProcessorGeneratedSourcesDirectory = file("src/main/generated")
}

tasks.clean {
    delete(file("src/main/generated"))
}

tasks.jar {
    enabled = true
    archiveClassifier = ""
}

tasks.bootJar {
    enabled = false
}
