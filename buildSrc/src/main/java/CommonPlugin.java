import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.logging.TestExceptionFormat;
import org.gradle.api.tasks.testing.logging.TestLogEvent;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.jetbrains.annotations.NotNull;

public class CommonPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project project) {
        applyPlugins(project);
        configureJava(project);
        configureDependencies(project);
        configureTest(project);
        configureJacoco(project);
    }

    private void applyPlugins(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(Plugins.SPRING_BOOT.getId());
        project.getPluginManager().apply(Plugins.SPRING_DEPENDENCY_MANAGEMENT.getId());
        project.getPluginManager().apply(JacocoPlugin.class);
        project.getPluginManager().apply(QueryDslPlugin.class);
    }

    private void configureJava(Project project) {
        project.getExtensions()
            .configure(JavaPluginExtension.class, java -> java.getToolchain()
                .getLanguageVersion()
                .set(JavaLanguageVersion.of(Version.JAVA_VERSION.getVersionAsInt())));

        // Explicitly enable annotation processing for both main and test source sets
        project.getTasks()
            .named("compileJava", JavaCompile.class)
            .configure(task -> task.getOptions().getCompilerArgs().addAll(Arrays.asList(
                "-parameters",
                "-Xlint:unchecked"
            )));

        project.getTasks()
            .named("compileTestJava", JavaCompile.class)
            .configure(task -> task.getOptions().getCompilerArgs().addAll(Arrays.asList(
                "-parameters",
                "-Xlint:unchecked"
            )));
    }

    private void configureDependencies(Project project) {
        project.getDependencies().add("implementation", Dependency.SPRING_BOOT_STARTER.getCoordinate());
        project.getDependencies().add("implementation", Dependency.SPRING_BOOT_STARTER_DATA_JPA.getCoordinate());
        project.getDependencies().add("compileOnly", Dependency.LOMBOK.getCoordinate());
        project.getDependencies().add("annotationProcessor", Dependency.LOMBOK.getCoordinate());
        project.getDependencies().add("testImplementation", Dependency.SPRING_BOOT_STARTER_TEST.getCoordinate());
        project.getDependencies().add("implementation", Dependency.SPRINGDOC_OPENAPI.getCoordinate());

        // Test database
        project.getDependencies().add("testRuntimeOnly", Dependency.H2_DATABASE.getCoordinate());

        // Test dependencies for Lombok
        project.getDependencies().add("testCompileOnly", Dependency.LOMBOK.getCoordinate());
        project.getDependencies().add("testAnnotationProcessor", Dependency.LOMBOK.getCoordinate());
    }

    private void configureTest(Project project) {
        project.getTasks().withType(Test.class).configureEach(test -> {
            test.useJUnitPlatform();
            test.finalizedBy(project.getTasks().named("jacocoTestReport"));

            test.setJvmArgs(Arrays.asList(
                "-XX:+EnableDynamicAgentLoading",
                "--add-opens=java.base/java.lang=ALL-UNNAMED"
            ));

            test.getTestLogging().events(
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.FAILED
            );
            test.getTestLogging().setExceptionFormat(TestExceptionFormat.FULL);
            test.getTestLogging().setShowExceptions(true);
            test.getTestLogging().setShowCauses(true);
            test.getTestLogging().setShowStackTraces(true);
        });
    }

    private void configureJacoco(Project project) {
        TaskProvider<JacocoReport> jacocoTestReport = project.getTasks().named("jacocoTestReport", JacocoReport.class);

        jacocoTestReport.configure(report -> {
            report.dependsOn(project.getTasks().named("test"));

            report.getReports().getXml().getRequired().set(true);
            report.getReports().getCsv().getRequired().set(true);
            report.getReports().getHtml().getRequired().set(true);

            report.getExecutionData().setFrom(
                project.fileTree(project.getLayout().getBuildDirectory().dir("jacoco")).include("**/*.exec")
            );

            report.getClassDirectories().setFrom(
                report.getClassDirectories().getFiles().stream()
                    .map(file -> project.fileTree(file, tree -> tree.exclude(
                        "**/config/**",
                        "**/entity/**",
                        "**/dto/**",
                        "**/exception/**",
                        "**/*Application*",
                        "**/Q*.class"
                    )))
                    .toArray()
            );

            report.finalizedBy("printCoverageReport");
        });

        TaskProvider<JacocoCoverageVerification> jacocoCoverageVerification =
            project.getTasks().named("jacocoTestCoverageVerification", JacocoCoverageVerification.class);

        jacocoCoverageVerification.configure(verification -> {
            verification.dependsOn(jacocoTestReport);

            verification.getViolationRules().rule(rule -> {
                rule.setElement("BUNDLE");

                rule.limit(limit -> {
                    limit.setCounter("LINE");
                    limit.setValue("COVEREDRATIO");
                    limit.setMinimum(new BigDecimal("0.70"));
                });

                rule.limit(limit -> {
                    limit.setCounter("BRANCH");
                    limit.setValue("COVEREDRATIO");
                    limit.setMinimum(new BigDecimal("0.60"));
                });

                rule.limit(limit -> {
                    limit.setCounter("CLASS");
                    limit.setValue("COVEREDRATIO");
                    limit.setMinimum(new BigDecimal("0.80"));
                });
            });

            verification.getClassDirectories().setFrom(
                verification.getClassDirectories().getFiles().stream()
                    .map(file -> project.fileTree(file, tree -> tree.exclude(
                        "**/config/**",
                        "**/entity/**",
                        "**/dto/**",
                        "**/exception/**",
                        "**/*Application*",
                        "**/Q*.class"
                    )))
                    .toArray()
            );
        });

        project.getTasks().named("check").configure(check -> check.dependsOn(jacocoCoverageVerification));

        project.getTasks().register("printCoverageReport", task -> {
            task.dependsOn(jacocoTestReport);
            task.doLast(t -> {
                File buildDir = project.getLayout().getBuildDirectory().get().getAsFile();
                File reportFile = new File(buildDir, "reports/jacoco/test/html/index.html");
                File xmlReportFile = new File(buildDir, "reports/jacoco/test/jacocoTestReport.xml");

                if (reportFile.exists()) {
                    System.out.println("📊 Coverage Report for " + project.getName() + ":");
                    System.out.println("   HTML: file://" + reportFile.getAbsolutePath());

                    if (xmlReportFile.exists()) {
                        System.out.println("   XML:  file://" + xmlReportFile.getAbsolutePath());

                        try {
                            String xmlContent = Files.readString(xmlReportFile.toPath());
                            Pattern linePattern = Pattern.compile(
                                "type=\"LINE\".*?covered=\"(\\d+)\".*?missed=\"(\\d+)\"");
                            Pattern branchPattern = Pattern.compile(
                                "type=\"BRANCH\".*?covered=\"(\\d+)\".*?missed=\"(\\d+)\"");

                            Matcher lineMatch = linePattern.matcher(xmlContent);
                            Matcher branchMatch = branchPattern.matcher(xmlContent);

                            if (lineMatch.find() && branchMatch.find()) {
                                int lineCovered = Integer.parseInt(lineMatch.group(1));
                                int lineMissed = Integer.parseInt(lineMatch.group(2));
                                int branchCovered = Integer.parseInt(branchMatch.group(1));
                                int branchMissed = Integer.parseInt(branchMatch.group(2));

                                int lineTotal = lineCovered + lineMissed;
                                int branchTotal = branchCovered + branchMissed;

                                if (lineTotal > 0) {
                                    double lineCoverage = (lineCovered * 100.0) / lineTotal;
                                    System.out.printf("   Line Coverage: %.2f%% (%d/%d)%n",
                                        lineCoverage, lineCovered, lineTotal);
                                }
                                if (branchTotal > 0) {
                                    double branchCoverage = (branchCovered * 100.0) / branchTotal;
                                    System.out.printf("   Branch Coverage: %.2f%% (%d/%d)%n",
                                        branchCoverage, branchCovered, branchTotal);
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("   Coverage details parsing failed: " + e.getMessage());
                        }
                    }
                    System.out.println();
                }
            });
        });
    }

}