package me.ele.lancet.weaver.internal.graph;

import me.ele.lancet.base.Scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gengwanpeng on 17/5/3.
 */
public class ClassNode extends Node {

    public List<ClassNode> children = Collections.emptyList();

    public ClassNode(String className) {
        super(new ClassEntity(className), null, Collections.emptyList());
    }

    @Override
    public CheckFlow.FlowNode toFlowNode(Scope scope) {
        CheckFlow.FlowNode node = new CheckFlow.FlowNode();
        recur(node, this, scope);
        return node;
    }

    private static void recur(CheckFlow.FlowNode node, ClassNode classNode, Scope scope) {
        node.className = classNode.entity.name;
        List<CheckFlow.FlowNode> list = node.children = new ArrayList<>(classNode.children.size());
        if (scope == Scope.LEAF || scope == Scope.ALL) {
            for (ClassNode child : classNode.children) {
                list.add(child.toFlowNode(scope));
            }
        }
    }
}
