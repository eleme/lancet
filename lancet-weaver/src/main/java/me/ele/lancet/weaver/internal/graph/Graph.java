package me.ele.lancet.weaver.internal.graph;

import me.ele.lancet.base.Scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by gengwanpeng on 17/5/5.
 */
public class Graph {


    private final Map<String, Node> nodeMap;

    public Graph(Map<String, Node> nodesMap) {
        this.nodeMap = nodesMap;
    }

    public void prepare() {
        nodeMap.values()
                .forEach(n -> {
                    if (n.parent != null) {
                        me.ele.lancet.weaver.internal.graph.ClassNode parent = n.parent;
                        if (parent.children == Collections.EMPTY_LIST) {
                            if (parent.className.equals("java/lang/Object")) {
                                parent.children = new ArrayList<>(nodeMap.size() >> 1);
                            } else {
                                parent.children = new ArrayList<>();
                            }
                            parent.children.add(n);
                        }
                    }
                    n.interfaces.forEach(i -> {
                        if (n instanceof InterfaceNode) {
                            if (i.children == Collections.EMPTY_LIST) {
                                i.children = new ArrayList<>();
                            }
                            i.children.add((InterfaceNode) n);
                        } else {
                            if (i.implementedClasses == Collections.EMPTY_LIST) {
                                i.implementedClasses = new ArrayList<>();
                            }
                            i.implementedClasses.add((me.ele.lancet.weaver.internal.graph.ClassNode) n);
                        }
                    });
                });
    }

    public boolean inherit(String child, String parent) {
        Node node = nodeMap.get(child);
        while (node != null && !parent.equals(node.className)) {
            node = node.parent;
        }
        return node != null;
    }

    /**
     * assert class always in nodeMap, if not, it's our code error.
     */
    public NodeVisitor childrenOf(String className, Scope scope) {
        return visitor -> {
            Node node = nodeMap.get(className);
            if (node instanceof InterfaceNode) {
                throw new IllegalArgumentException(className + " is a interface");
            }
            visitClasses((ClassNode) node, scope, visitor);
        };
    }

    private void visitClasses(ClassNode parent, Scope scope, Consumer<Node> visitor) {
        List<Node> children = parent.children;
        switch (scope) {
            case ALL:
                children.stream()
                        .peek(visitor)
                        .filter(s -> s instanceof ClassNode)
                        .forEach(n -> visitClasses((ClassNode) n, scope, visitor));
                break;
            case DIRECT:
                children.forEach(visitor);
                break;
            case LEAF:
                children.stream()
                        .filter(n -> {
                            if (n instanceof InterfaceNode || ((ClassNode) n).children.size() == 0) {
                                visitor.accept(n);
                                return false;
                            }
                            return true;
                        })
                        .forEach(n -> visitClasses((ClassNode) n, scope, visitor));
                break;
        }
    }

    public NodeVisitor implementsOf(String[] interfaces, Scope scope) {
        return visitor -> {
            for (String it : interfaces) {
                Node node = nodeMap.get(it);
                if (node instanceof ClassNode) {
                    throw new IllegalArgumentException(it + " is a class");
                }
                visitImplements((InterfaceNode) node, scope, visitor);
            }
        };
    }

    private void visitImplements(InterfaceNode node, Scope scope, Consumer<Node> visitor) {
        List<ClassNode> classes = node.implementedClasses;
        List<InterfaceNode> children = node.children;
        switch (scope) {
            case ALL:
                children.forEach(c -> visitImplements(c, scope, visitor));
            case DIRECT:
                classes.forEach(visitor);
                break;
            case LEAF:
                children.forEach(c -> visitImplements(c, scope, visitor));
                classes.stream()
                        .filter(c -> {
                            if (c.children.size() <= 0) {
                                visitor.accept(c);
                                return false;
                            }
                            return true;
                        })
                        .forEach(c -> visitClasses(c, scope, visitor));
        }
    }


    public interface NodeVisitor {
        void forEach(Consumer<Node> node);
    }
}
