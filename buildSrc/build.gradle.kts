plugins {
    `java-gradle-plugin`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("gradle.plugin.com.ewerk.gradle.plugins:querydsl-plugin:1.0.10")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}