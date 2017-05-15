package me.ele.lancet.weaver.internal.graph;

import java.util.Collections;
import java.util.List;

/**
 * Created by gengwanpeng on 17/5/3.
 */
public class InterfaceNode extends Node {

    public List<InterfaceNode> children = Collections.emptyList();
    public List<ClassNode> implementedClasses = Collections.emptyList();

    public InterfaceNode(String className) {
        super(new ClassEntity(className), null, Collections.emptyList());
    }
}
