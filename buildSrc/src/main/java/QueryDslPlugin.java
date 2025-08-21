import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;

public class QueryDslPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project project) {
        configureDependencies(project);
        configureSourceSets(project);
        configureAnnotationProcessing(project);
        configureCleanTask(project);
    }

    private void configureDependencies(Project project) {
        project.getDependencies().add("implementation", Dependency.QUERYDSL_JPA.getCoordinate());
        project.getDependencies().add("annotationProcessor", Dependency.QUERYDSL_APT.getCoordinate());
        project.getDependencies().add("annotationProcessor", Dependency.JAKARTA_ANNOTATION.getCoordinate());
        project.getDependencies().add("annotationProcessor", Dependency.JAKARTA_PERSISTENCE.getCoordinate());

        // Test dependencies
        project.getDependencies().add("testAnnotationProcessor", Dependency.QUERYDSL_APT.getCoordinate());
        project.getDependencies().add("testAnnotationProcessor", Dependency.JAKARTA_ANNOTATION.getCoordinate());
        project.getDependencies().add("testAnnotationProcessor", Dependency.JAKARTA_PERSISTENCE.getCoordinate());
    }

    private void configureSourceSets(Project project) {
        JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);

        // Use standard Gradle generated sources path for better IDE support
        File qdir = new File(project.getLayout().getBuildDirectory().getAsFile().get(),
            "generated/sources/annotationProcessor/java/main");

        // Add generated directory to main source set
        javaExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getJava()
            .srcDir(qdir);

        // Add generated directory to test source set so tests can access Q classes
        javaExtension.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME).getJava()
            .srcDir(qdir);
    }

    private void configureAnnotationProcessing(Project project) {
        // Use standard Gradle generated sources path for better IDE support
        File qdir = new File(project.getLayout().getBuildDirectory().getAsFile().get(),
            "generated/sources/annotationProcessor/java/main");

        project.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> {
            javaCompile.getOptions().getGeneratedSourceOutputDirectory().set(qdir);
            // Remove explicit -processor option to allow automatic processor discovery
            // This enables both QueryDSL and Lombok processors to work together
        });
    }

    private void configureCleanTask(Project project) {
        // Use standard Gradle generated sources path for better IDE support
        File qdir = new File(project.getLayout().getBuildDirectory().getAsFile().get(),
            "generated/sources/annotationProcessor/java/main");

        project.getTasks().named("clean").configure(cleanTask -> cleanTask.doFirst(task -> project.delete(qdir)));
    }

}
