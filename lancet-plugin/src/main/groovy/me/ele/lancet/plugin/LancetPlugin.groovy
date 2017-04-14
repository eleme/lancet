package me.ele.lancet.plugin

import com.android.build.gradle.AppExtension
import me.ele.lancet.base.PlaceHolder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException

class LancetPlugin implements Plugin<Project> {

    void apply(Project project) {
        if (!project.plugins.findPlugin('android') && !project.plugins.findPlugin('com.android.application')) {
            throw new ProjectConfigurationException("Need android application plugin to be applied first", null)
        }

        def appExtension = project.android as AppExtension
        def lancetExtension = project.extensions.create 'lancet', LancetExtension
        appExtension.registerTransform new LancetTransform(lancetExtension, appExtension)

        project.gradle.afterProject {
            if (it == project) {
                appExtension.packagingOptions.exclude("/" + PlaceHolder.RESOURCE_DIR + "**")
            }
        }
    }
}
