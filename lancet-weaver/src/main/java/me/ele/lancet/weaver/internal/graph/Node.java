package me.ele.lancet.weaver.internal.graph;

import com.android.build.api.transform.Status;
import me.ele.lancet.base.Scope;

import java.util.Collections;
import java.util.List;

/**
 * Created by gengwanpeng on 17/5/2.
 */
public abstract class Node {

    // for flow check
    public boolean[] checkPass = new boolean[2];
    public Status status;


    public Node(ClassEntity entity, ClassNode parent, List<InterfaceNode> interfaces) {
        this.entity = entity;
        this.parent = parent;
        this.interfaces = interfaces;
    }

    public abstract CheckFlow.FlowNode toFlowNode(Scope scope);

    public ClassNode parent; // null means it doesn't exists actually, it's a virtual class node
    public List<InterfaceNode> interfaces;

    public ClassEntity entity;

    public static Node newPlaceHolder(String className) {
        return new Node(new ClassEntity(className), null, Collections.emptyList()) {
            @Override
            public CheckFlow.FlowNode toFlowNode(Scope scope) {
                return null;
            }
        };
    }
}
