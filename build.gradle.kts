plugins {
    id(Plugins.JAVA.id)
    id(Plugins.SPRING_BOOT.id) version Plugins.SPRING_BOOT.version apply false
    id(Plugins.SPRING_DEPENDENCY_MANAGEMENT.id) version Plugins.SPRING_DEPENDENCY_MANAGEMENT.version apply false
    id(Plugins.SONARQUBE.id) version Plugins.SONARQUBE.version
    id(Plugins.FLYWAY.id) version Plugins.FLYWAY.version
    id(Plugins.JACOCO.id)
}

// Flyway plugin dependencies
buildscript {
    dependencies {
        classpath(BuildDependencies.getMysqlConnector())
        classpath(BuildDependencies.getFlywayMysql())
    }
}

allprojects {
    group = "com.msa.commerce"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply<CommonPlugin>()

    // Common configurations are now handled by CommonPlugin
}

//// SonarQube 설정 - SonarCloud 분석을 위한 구성
//sonar {
//    properties {
//        property("sonar.projectName", "MSA Commerce Lab")
//        property("sonar.projectKey", "msa-commerce-lab")
//        property("sonar.organization", System.getenv("SONAR_ORGANIZATION") ?: "your-org")
//        property("sonar.host.url", "https://sonarcloud.io")
//
//        // 언어 및 인코딩 설정
//        property("sonar.language", "java")
//        property("sonar.sourceEncoding", "UTF-8")
//
//        // 소스 및 테스트 디렉토리
//        property("sonar.sources", subprojects.joinToString(",") { "${it.name}/src/main" })
//        property("sonar.tests", subprojects.joinToString(",") { "${it.name}/src/test" })
//
//        // 바이너리 파일 경로
//        property("sonar.java.binaries", subprojects.joinToString(",") { "${it.name}/build/classes" })
//
//        // 테스트 포함 패턴
//        property("sonar.test.inclusions", "**/*Test.java,**/*Tests.java,**/*IT.java")
//
//        // 분석 제외 패턴
//        property("sonar.exclusions", listOf(
//            "**/config/**",
//            "**/entity/**",
//            "**/dto/**",
//            "**/exception/**",
//            "**/*Application*",
//            "**/Q*.java", // QueryDSL generated classes
//            "**/build/**",
//            "**/gradle/**"
//        ).joinToString(","))
//
//        // 커버리지 제외 패턴
//        property("sonar.coverage.exclusions", listOf(
//            "**/config/**",
//            "**/dto/**",
//            "**/entity/**",
//            "**/exception/**",
//            "**/*Application*",
//            "**/Q*.java",
//            "**/*Config.java",
//            "**/*Configuration.java"
//        ).joinToString(","))
//
//        // Jacoco 커버리지 리포트 통합
//        property("sonar.coverage.jacoco.xmlReportPaths",
//            "build/reports/jacoco/jacocoRootReport/jacocoRootReport.xml," +
//            subprojects.joinToString(",") { "${it.name}/build/reports/jacoco/test/jacocoTestReport.xml" }
//        )
//
//        // 코드 품질 기준 설정
//        property("sonar.qualitygate.wait", "true")
//
//        // 중복 코드 감지 설정
//        property("sonar.cpd.java.minimumtokens", "50")
//
//        // 이슈 심각도 임계값
//        property("sonar.issue.ignore.multicriteria", "e1,e2")
//        property("sonar.issue.ignore.multicriteria.e1.ruleKey", "java:S1186")
//        property("sonar.issue.ignore.multicriteria.e1.resourceKey", "**/*Test*.java")
//        property("sonar.issue.ignore.multicriteria.e2.ruleKey", "java:S2699")
//        property("sonar.issue.ignore.multicriteria.e2.resourceKey", "**/*Test*.java")
//    }
//}

// 전체 모듈 통합 커버리지 리포트 태스크
tasks.register<JacocoReport>("jacocoRootReport") {
    description = "Generate integrated coverage report for all modules"
    group = "verification"

    dependsOn(subprojects.map { it.tasks.named("test") })

    additionalSourceDirs.setFrom(subprojects.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    sourceDirectories.setFrom(subprojects.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    classDirectories.setFrom(subprojects.map {
        it.the<SourceSetContainer>()["main"].output.classesDirs.map { classDir ->
            fileTree(classDir) {
                exclude(
                    "**/config/**",
                    "**/entity/**",
                    "**/dto/**",
                    "**/exception/**",
                    "**/*Application*",
                    "**/Q*.class"
                )
            }
        }
    })

    executionData.setFrom(subprojects.map {
        it.fileTree("${it.layout.buildDirectory.get().asFile}/jacoco").include("**/*.exec")
    })

    reports {
        xml.required = true
        html.required = true
        csv.required = true

        xml.outputLocation = file("${layout.buildDirectory.get().asFile}/reports/jacoco/jacocoRootReport/jacocoRootReport.xml")
        html.outputLocation = file("${layout.buildDirectory.get().asFile}/reports/jacoco/jacocoRootReport/html")
        csv.outputLocation = file("${layout.buildDirectory.get().asFile}/reports/jacoco/jacocoRootReport/jacocoRootReport.csv")
    }

    finalizedBy("printRootCoverageReport")
}

tasks.register("printRootCoverageReport") {
    description = "Print integrated coverage report summary"
    group = "verification"

    doLast {
        val htmlReportFile = file("${layout.buildDirectory.get().asFile}/reports/jacoco/jacocoRootReport/html/index.html")
        val xmlReportFile = file("${layout.buildDirectory.get().asFile}/reports/jacoco/jacocoRootReport/jacocoRootReport.xml")

        if (htmlReportFile.exists()) {
            println("=".repeat(50))
            println("📊 INTEGRATED COVERAGE REPORT")
            println("=".repeat(50))
            println("HTML Report: file://${htmlReportFile.absolutePath}")
            println("XML Report:  file://${xmlReportFile.absolutePath}")

            if (xmlReportFile.exists()) {
                try {
                    val xmlContent = xmlReportFile.readText()
                    val lineMatch = Regex("""type="LINE".*?covered="(\d+)".*?missed="(\d+)"""").find(xmlContent)
                    val branchMatch = Regex("""type="BRANCH".*?covered="(\d+)".*?missed="(\d+)"""").find(xmlContent)
                    val classMatch = Regex("""type="CLASS".*?covered="(\d+)".*?missed="(\d+)"""").find(xmlContent)
                    val methodMatch = Regex("""type="METHOD".*?covered="(\d+)".*?missed="(\d+)"""").find(xmlContent)

                    println()
                    println("Coverage Summary:")
                    println("-".repeat(30))

                    if (lineMatch != null) {
                        val covered = lineMatch.groupValues[1].toInt()
                        val missed = lineMatch.groupValues[2].toInt()
                        val total = covered + missed
                        if (total > 0) {
                            val percentage = (covered.toDouble() / total * 100)
                            println("Lines:    ${"%.2f".format(percentage)}% ($covered/$total)")
                        }
                    }

                    if (branchMatch != null) {
                        val covered = branchMatch.groupValues[1].toInt()
                        val missed = branchMatch.groupValues[2].toInt()
                        val total = covered + missed
                        if (total > 0) {
                            val percentage = (covered.toDouble() / total * 100)
                            println("Branches: ${"%.2f".format(percentage)}% ($covered/$total)")
                        }
                    }

                    if (classMatch != null) {
                        val covered = classMatch.groupValues[1].toInt()
                        val missed = classMatch.groupValues[2].toInt()
                        val total = covered + missed
                        if (total > 0) {
                            val percentage = (covered.toDouble() / total * 100)
                            println("Classes:  ${"%.2f".format(percentage)}% ($covered/$total)")
                        }
                    }

                    if (methodMatch != null) {
                        val covered = methodMatch.groupValues[1].toInt()
                        val missed = methodMatch.groupValues[2].toInt()
                        val total = covered + missed
                        if (total > 0) {
                            val percentage = (covered.toDouble() / total * 100)
                            println("Methods:  ${"%.2f".format(percentage)}% ($covered/$total)")
                        }
                    }

                } catch (e: Exception) {
                    println("Coverage summary parsing failed: ${e.message}")
                }
            }
            println("=".repeat(50))
        }
    }
}

// 통합 커버리지 검증 태스크
tasks.register<JacocoCoverageVerification>("jacocoRootCoverageVerification") {
    description = "Verify integrated coverage meets minimum thresholds"
    group = "verification"

    dependsOn("jacocoRootReport")

    violationRules {
        rule {
            element = "BUNDLE"

            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.70".toBigDecimal()
            }

            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.60".toBigDecimal()
            }

            limit {
                counter = "CLASS"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }

    classDirectories.setFrom(subprojects.map {
        it.the<SourceSetContainer>()["main"].output.classesDirs.map { classDir ->
            fileTree(classDir) {
                exclude(
                    "**/config/**",
                    "**/entity/**",
                    "**/dto/**",
                    "**/exception/**",
                    "**/*Application*",
                    "**/Q*.class"
                )
            }
        }
    })

    executionData.setFrom(subprojects.map {
        it.fileTree("${it.layout.buildDirectory.get().asFile}/jacoco").include("**/*.exec")
    })
}

// 편의 태스크들
tasks.register("testWithCoverage") {
    description = "Run all tests and generate coverage reports"
    group = "verification"

    dependsOn("test", "jacocoTestReport", "jacocoRootReport")
}

tasks.register("coverageReport") {
    description = "Generate and display coverage reports"
    group = "verification"

    dependsOn("jacocoRootReport")
}

tasks.register("verifyCoverage") {
    description = "Verify all coverage thresholds"
    group = "verification"

    dependsOn("jacocoTestCoverageVerification", "jacocoRootCoverageVerification")
}

// ============================================================================
// Centralized Flyway Configuration for All Databases
// ============================================================================

flyway {
    url = "jdbc:mysql://localhost:3306"
    user = "app_flyway"
    password = "1q2w3e4r!@"
    
    // Use centralized migration location
    locations = arrayOf("filesystem:infra/db/migration")
    
    // Flyway configuration
    baselineOnMigrate = true
    validateOnMigrate = true
    cleanDisabled = false
    
    schemas = arrayOf("db_flyway", "db_platform", "db_order", "db_payment")

    // Placeholder configuration
    placeholderReplacement = true
}
