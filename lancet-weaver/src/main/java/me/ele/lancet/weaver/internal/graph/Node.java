package me.ele.lancet.weaver.internal.graph;

import java.util.List;

/**
 * Created by gengwanpeng on 17/5/2.
 */
public abstract class Node {

    public Node(ClassEntity entity, ClassNode parent, List<InterfaceNode> interfaces) {
        this.entity = entity;
        this.parent = parent;
        this.interfaces = interfaces;
    }

    public ClassNode parent; // null means it doesn't exists actually, it's a virtual class node
    public List<InterfaceNode> interfaces;

    public ClassEntity entity;
}
