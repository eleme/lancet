package me.ele.lancet.parser;

import com.android.build.gradle.LibraryExtension;
import com.android.utils.StringHelper;

import org.gradle.BuildAdapter;
import org.gradle.api.*;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.tasks.bundling.Zip;

import java.io.IOException;

/**
 * Created by gengwanpeng on 17/4/11.
 */
public class ParseClassNamePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.afterEvaluate(p -> {
            if (project.getPlugins().findPlugin("com.android.application") != null) {
                throw new GradleException("the plugin is incompatible with com.android.application");
            }

            if (project.getPlugins().findPlugin("com.android.library") != null) {
                LibraryExtension extension = (LibraryExtension) project.getExtensions().getByName("android");
                //for eleme eradle
                project.getGradle().addBuildListener(new BuildAdapter(){
                    @Override
                    public void projectsEvaluated(Gradle gradle) {
                        extension.getLibraryVariants().all(l -> {
                            String variant = StringHelper.capitalize(l.getName());
                            project.getTasks().getByName("bundle" + variant).doLast(new ZipAction());
                            Zip zip = (Zip) project.getTasks().findByName("jar" + variant);
                            if(zip != null){
                                zip.doLast(new ZipAction());
                            }
                        });
                    }
                });
            } else {
                project.getTasks().getByName("jar").doLast(new ZipAction());
            }
        });
    }

    private static class ZipAction implements Action<Task> {

        @Override
        public void execute(Task task) {
            Zip zip = (Zip) task;
            try {
                new ParseClassNameTask(zip.getArchivePath(), zip.getProject().getBuildDir()).run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
