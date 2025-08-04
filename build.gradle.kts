plugins {
    java
    id("org.springframework.boot") version "3.3.4" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("org.sonarqube") version "6.2.0.5505"
    jacoco
}

allprojects {
    group = "com.msa.commerce"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "jacoco")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter")
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)

        // 테스트 실행 시 JVM 옵션 설정
        jvmArgs = listOf(
            "-XX:+EnableDynamicAgentLoading",
            "--add-opens=java.base/java.lang=ALL-UNNAMED"
        )

        // 테스트 결과 리포트 개선
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required = true
            csv.required = true
            html.required = true
        }

        executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec"))

        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/config/**",
                        "**/entity/**",
                        "**/dto/**",
                        "**/exception/**",
                        "**/*Application*",
                        "**/Q*.class"
                    )
                }
            })
        )

        finalizedBy("printCoverageReport")
    }

    tasks.jacocoTestCoverageVerification {
        dependsOn(tasks.jacocoTestReport)

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

        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/config/**",
                        "**/entity/**",
                        "**/dto/**",
                        "**/exception/**",
                        "**/*Application*",
                        "**/Q*.class"
                    )
                }
            })
        )
    }

    // 빌드 시 커버리지 검증 포함
    tasks.check {
        dependsOn(tasks.jacocoTestCoverageVerification)
    }

    // 커버리지 리포트 후 결과 출력
    tasks.register("printCoverageReport") {
        dependsOn(tasks.jacocoTestReport)
        doLast {
            val reportFile = file("${layout.buildDirectory.get().asFile}/reports/jacoco/test/html/index.html")
            val xmlReportFile = file("${layout.buildDirectory.get().asFile}/reports/jacoco/test/jacocoTestReport.xml")

            if (reportFile.exists()) {
                println("📊 Coverage Report for ${project.name}:")
                println("   HTML: file://${reportFile.absolutePath}")
                if (xmlReportFile.exists()) {
                    println("   XML:  file://${xmlReportFile.absolutePath}")

                    // XML에서 간단한 커버리지 정보 추출
                    try {
                        val xmlContent = xmlReportFile.readText()
                        val lineMatch = Regex("""type="LINE".*?covered="(\d+)".*?missed="(\d+)"""").find(xmlContent)
                        val branchMatch = Regex("""type="BRANCH".*?covered="(\d+)".*?missed="(\d+)"""").find(xmlContent)

                        if (lineMatch != null && branchMatch != null) {
                            val lineCovered = lineMatch.groupValues[1].toInt()
                            val lineMissed = lineMatch.groupValues[2].toInt()
                            val branchCovered = branchMatch.groupValues[1].toInt()
                            val branchMissed = branchMatch.groupValues[2].toInt()

                            val lineTotal = lineCovered + lineMissed
                            val branchTotal = branchCovered + branchMissed

                            if (lineTotal > 0) {
                                val lineCoverage = (lineCovered.toDouble() / lineTotal * 100)
                                println("   Line Coverage: ${"%.2f".format(lineCoverage)}% ($lineCovered/$lineTotal)")
                            }
                            if (branchTotal > 0) {
                                val branchCoverage = (branchCovered.toDouble() / branchTotal * 100)
                                println("   Branch Coverage: ${"%.2f".format(branchCoverage)}% ($branchCovered/$branchTotal)")
                            }
                        }
                    } catch (e: Exception) {
                        println("   Coverage details parsing failed: ${e.message}")
                    }
                }
                println()
            }
        }
    }
}

// SonarQube 설정 - SonarCloud 분석을 위한 구성
sonar {
    properties {
        property("sonar.projectName", "MSA Commerce Lab")
        property("sonar.projectKey", "msa-commerce-lab")
        property("sonar.organization", System.getenv("SONAR_ORGANIZATION") ?: "your-org")
        property("sonar.host.url", "https://sonarcloud.io")

        // 언어 및 인코딩 설정
        property("sonar.language", "java")
        property("sonar.sourceEncoding", "UTF-8")

        // 소스 및 테스트 디렉토리
        property("sonar.sources", subprojects.joinToString(",") { "${it.name}/src/main" })
        property("sonar.tests", subprojects.joinToString(",") { "${it.name}/src/test" })

        // 바이너리 파일 경로
        property("sonar.java.binaries", subprojects.joinToString(",") { "${it.name}/build/classes" })

        // 테스트 포함 패턴
        property("sonar.test.inclusions", "**/*Test.java,**/*Tests.java,**/*IT.java")

        // 분석 제외 패턴
        property("sonar.exclusions", listOf(
            "**/config/**",
            "**/entity/**",
            "**/dto/**",
            "**/exception/**",
            "**/*Application*",
            "**/Q*.java", // QueryDSL generated classes
            "**/build/**",
            "**/gradle/**"
        ).joinToString(","))

        // 커버리지 제외 패턴
        property("sonar.coverage.exclusions", listOf(
            "**/config/**",
            "**/dto/**",
            "**/entity/**",
            "**/exception/**",
            "**/*Application*",
            "**/Q*.java",
            "**/*Config.java",
            "**/*Configuration.java"
        ).joinToString(","))

        // Jacoco 커버리지 리포트 통합
        property("sonar.coverage.jacoco.xmlReportPaths",
            "build/reports/jacoco/jacocoRootReport/jacocoRootReport.xml," +
            subprojects.joinToString(",") { "${it.name}/build/reports/jacoco/test/jacocoTestReport.xml" }
        )

        // 코드 품질 기준 설정
        property("sonar.qualitygate.wait", "true")

        // 중복 코드 감지 설정
        property("sonar.cpd.java.minimumtokens", "50")

        // 이슈 심각도 임계값
        property("sonar.issue.ignore.multicriteria", "e1,e2")
        property("sonar.issue.ignore.multicriteria.e1.ruleKey", "java:S1186")
        property("sonar.issue.ignore.multicriteria.e1.resourceKey", "**/*Test*.java")
        property("sonar.issue.ignore.multicriteria.e2.ruleKey", "java:S2699")
        property("sonar.issue.ignore.multicriteria.e2.resourceKey", "**/*Test*.java")
    }
}

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
