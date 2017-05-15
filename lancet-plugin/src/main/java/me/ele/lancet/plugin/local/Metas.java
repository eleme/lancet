package me.ele.lancet.plugin.local;


import me.ele.lancet.weaver.internal.graph.ClassEntity;

import java.util.Collections;
import java.util.List;

/**
 * Created by gengwanpeng on 17/4/26.
 */
class Metas {

    public List<String> classes = Collections.emptyList();

    public List<String> classesInDirs = Collections.emptyList();

    public List<String> jarsWithHookClasses = Collections.emptyList();

    public List<ClassEntity> classMetas = Collections.emptyList();
}
