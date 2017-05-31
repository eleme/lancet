package me.ele.lancet.plugin.internal;


import me.ele.lancet.weaver.internal.graph.ClassEntity;
import me.ele.lancet.weaver.internal.graph.CheckFlow;

import java.util.Collections;
import java.util.List;

/**
 * Created by gengwanpeng on 17/4/26.
 */
class Metas {

    public List<String> classes = Collections.emptyList();

    public List<String> classesInDirs = Collections.emptyList();

    public List<String> jarsWithHookClasses = Collections.emptyList();

    public CheckFlow flow = new CheckFlow();

    public List<ClassEntity> classMetas = Collections.emptyList();


    public Metas withoutNull() {
        Metas shallowClone = new Metas();
        if (classes != null) {
            shallowClone.classes = classes;
        }
        if (classesInDirs != null) {
            shallowClone.classesInDirs = classesInDirs;
        }
        if (jarsWithHookClasses != null) {
            shallowClone.jarsWithHookClasses = jarsWithHookClasses;
        }
        if (classMetas != null) {
            shallowClone.classMetas = classMetas;
        }
        if (flow != null) {
            shallowClone.flow = flow;
        }
        return shallowClone;
    }
}
