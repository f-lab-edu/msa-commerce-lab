import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class QueryDslPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project project) {
        applyPlugins(project);
        configureDependencies(project);
    }

    private void applyPlugins(Project project) {
        project.getPluginManager().apply(Plugins.QUERYDSL.getId());
    }

    private void configureDependencies(Project project) {
        project.getDependencies().add("api", Dependency.QUERYDSL_JPA.getCoordinate());
        project.getDependencies().add("annotationProcessor", Dependency.QUERYDSL_APT.getCoordinate());
        project.getDependencies().add("annotationProcessor", Dependency.JAKARTA_ANNOTATION.getCoordinate());
        project.getDependencies().add("annotationProcessor", Dependency.JAKARTA_PERSISTENCE.getCoordinate());
    }

}
