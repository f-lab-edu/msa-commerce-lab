import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.jetbrains.annotations.NotNull;

public class JavaLibraryPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project project) {
        applyPlugins(project);
        configureJava(project);
        configureDependencies(project);
        configureTasks(project);
    }

    private void applyPlugins(Project project) {
        project.getPluginManager().apply(Plugins.JAVA_LIBRARY.getId());
        project.getPluginManager().apply(Plugins.SPRING_BOOT.getId());
        project.getPluginManager().apply(Plugins.SPRING_DEPENDENCY_MANAGEMENT.getId());
        project.getPluginManager().apply(JacocoPlugin.class);
    }

    private void configureJava(Project project) {
        project.getExtensions()
            .configure(JavaPluginExtension.class, java -> java.getToolchain()
                .getLanguageVersion()
                .set(JavaLanguageVersion.of(Version.JAVA_VERSION.getVersionAsInt())));

        project.getConfigurations()
            .named("compileOnly")
            .configure(
                compileOnly -> compileOnly.extendsFrom(project.getConfigurations().named("annotationProcessor").get()));
    }

    private void configureDependencies(Project project) {
        project.getDependencies().add("api", Dependency.SPRING_BOOT_STARTER_WEB.getCoordinate());
        project.getDependencies().add("api", Dependency.SPRING_BOOT_STARTER_DATA_JPA.getCoordinate());
        project.getDependencies().add("api", Dependency.SPRING_BOOT_STARTER_VALIDATION.getCoordinate());
        project.getDependencies().add("api", Dependency.SPRING_BOOT_STARTER_DATA_REDIS.getCoordinate());
        project.getDependencies().add("api", Dependency.MYSQL_CONNECTOR.getCoordinate());

        project.getDependencies().add("compileOnly", Dependency.LOMBOK.getCoordinate());
        project.getDependencies().add("annotationProcessor", Dependency.LOMBOK.getCoordinate());
        project.getDependencies().add("testImplementation", Dependency.SPRING_BOOT_STARTER_TEST.getCoordinate());
        project.getDependencies().add("api", Dependency.SPRING_KAFKA.getCoordinate());

        project.getDependencies().add("testImplementation", Dependency.H2_DATABASE.getCoordinate());
        project.getDependencies().add("testImplementation", Dependency.SPRING_KAFKA_TEST.getCoordinate());
        project.getDependencies().add("testImplementation", Dependency.AWAITILITY.getCoordinate());
        project.getDependencies().add("testImplementation", Dependency.TESTCONTAINERS_JUNIT.getCoordinate());
        project.getDependencies().add("testImplementation", Dependency.TESTCONTAINERS_MYSQL.getCoordinate());
    }

    private void configureTasks(Project project) {
        project.getTasks().named("jar", Jar.class).configure(jar -> {
            jar.setEnabled(true);
            jar.getArchiveClassifier().set("");
        });

        project.getTasks().named("bootJar").configure(bootJar -> bootJar.setEnabled(false));
    }

}
