package me.ele.lancet.plugin.internal;

import org.gradle.api.Project;

import java.io.File;

/**
 * Created by gengwanpeng on 17/4/26.
 */
public class GlobalContext {

    private Project project;

    public GlobalContext(Project project) {
        this.project = project;
    }


    public File getLancetDir() {
        return new File(project.getBuildDir(), "lancet");
    }
}
