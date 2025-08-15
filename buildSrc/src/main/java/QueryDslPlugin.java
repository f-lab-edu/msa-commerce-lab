import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Custom QueryDSL plugin that provides complete QueryDSL configuration
 * without relying on external plugins
 */
public class QueryDslPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project project) {
        configureDependencies(project);
        configureSourceSets(project);
        configureAnnotationProcessing(project);
        configureCleanTask(project);
    }

    /**
     * Add QueryDSL dependencies
     */
    private void configureDependencies(Project project) {
        project.getDependencies().add("api", Dependency.QUERYDSL_JPA.getCoordinate());
        project.getDependencies().add("annotationProcessor", Dependency.QUERYDSL_APT.getCoordinate());
        project.getDependencies().add("annotationProcessor", Dependency.JAKARTA_ANNOTATION.getCoordinate());
        project.getDependencies().add("annotationProcessor", Dependency.JAKARTA_PERSISTENCE.getCoordinate());
    }

    /**
     * Configure source sets to include generated sources
     */
    private void configureSourceSets(Project project) {
        JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        File generatedDir = new File(project.getProjectDir(), "src/main/generated");
        
        javaExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getJava()
            .srcDir(generatedDir);
    }

    /**
     * Configure annotation processing for QueryDSL
     */
    private void configureAnnotationProcessing(Project project) {
        File generatedDir = new File(project.getProjectDir(), "src/main/generated");
        
        project.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> {
            javaCompile.getOptions().getGeneratedSourceOutputDirectory().set(generatedDir);
        });
    }

    /**
     * Configure clean task to remove generated sources
     */
    private void configureCleanTask(Project project) {
        File generatedDir = new File(project.getProjectDir(), "src/main/generated");
        
        project.getTasks().named("clean").configure(cleanTask -> {
            cleanTask.doFirst(task -> {
                project.delete(generatedDir);
            });
        });
    }
}
