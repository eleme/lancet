package me.ele.lancet.plugin.local;

import java.util.Collections;
import java.util.List;

/**
 * Created by gengwanpeng on 17/4/26.
 */
class Metas {

    public List<String> classes = Collections.emptyList();

    public List<String> classesInDirs = Collections.emptyList();

    public List<String> jarsWithHookClasses = Collections.emptyList();

    public List<NodeLike> nodeLikes = Collections.emptyList();


    public static class NodeLike {

        public int access;
        public String name;
        public String superName;
        public String[] interfaces;
    }
}
