package me.ele.lancet.plugin;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;

public class LancetPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        if (project.getPlugins().findPlugin("android") == null && project.getPlugins().findPlugin("com.android.application") == null) {
            throw new ProjectConfigurationException("Need android application plugin to be applied first", null);
        }

        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
        LancetExtension lancetExtension = project.getExtensions().create("lancet", LancetExtension.class);
        appExtension.registerTransform(new LancetTransform(project, lancetExtension, appExtension));

        /*project.getGradle().afterProject(new Closure(project) {

            @Override
            public void run() {
                appExtension.getPackagingOptions().exclude("/" + Origin.RESOURCE_DIR + "**");
            }
        });*/
    }
}
