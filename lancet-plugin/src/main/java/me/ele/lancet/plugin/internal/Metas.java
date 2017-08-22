package me.ele.lancet.plugin.internal;


import java.util.Collections;
import java.util.List;

import me.ele.lancet.weaver.internal.graph.CheckFlow;
import me.ele.lancet.weaver.internal.graph.ClassEntity;

/**
 * Created by gengwanpeng on 17/4/26.
 */
class Metas {

    public List<String> hookClasses = Collections.emptyList();

    public List<String> hookClassesInDir = Collections.emptyList();

    public List<String> jarsWithHookClasses = Collections.emptyList();

    public CheckFlow flow = new CheckFlow();

    public List<ClassEntity> classMetas = Collections.emptyList();


    public Metas withoutNull() {
        Metas shallowClone = new Metas();
        if (hookClasses != null) {
            shallowClone.hookClasses = hookClasses;
        }
        if (hookClassesInDir != null) {
            shallowClone.hookClassesInDir = hookClassesInDir;
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
