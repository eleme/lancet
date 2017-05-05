package me.ele.lancet.weaver.internal.graph;

import java.util.Collections;
import java.util.List;

/**
 * Created by gengwanpeng on 17/5/3.
 */
public class ClassNode extends Node {

    public List<Node> children = Collections.emptyList();

    public ClassNode(String className) {
        super(0, className, null, Collections.emptyList());
    }
}
