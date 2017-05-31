package me.ele.lancet.weaver.internal.graph;

import com.android.build.api.transform.Status;
import me.ele.lancet.base.Scope;
import me.ele.lancet.weaver.internal.log.Log;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by gengwanpeng on 17/5/22.
 */
public class CheckFlow {

    public Map<String, FlowEntity> map = new HashMap<>();

    public void add(Graph graph, String className, Scope scope) {
        Node node = graph.get(className);
        if (node != null && !map.containsKey(className)) {
            map.put(className, new FlowEntity(node.toFlowNode(scope)));
        }
    }

    public void exactlyMatch(String className) {
        FlowEntity entity = map.get(className);
        if (entity != null) {
            entity.exactMatch = true;
        }
    }

    public boolean isIncremental(Graph graph) {
        return map.values().stream().allMatch(f -> {
            Node n = graph.get(f.root.className);
            return n != null && f.check(n, graph);
        });
    }

    public void clear() {
        map.clear();
    }

    public static class FlowEntity {

        public boolean exactMatch = false;
        public FlowNode root;

        public FlowEntity(FlowNode root) {
            this.root = root;
        }

        public boolean check(Node n, Graph graph) {
            if (exactMatch) {
                return root.checkExactly(n);
            }
            return root.checkNormally(n, graph);
        }
    }


    public static class FlowNode {
        public String className;

        public List<FlowNode> children = Collections.emptyList();

        // TODO: This grain is too coarse. Now, we just judge the extend and implements flow, check if some classes lost.
        public boolean checkExactly(Node n) {
            if (n.checkPass[0]) {
                return true;
            }
            Map<String, Node> map = directChildrenOf(n);
            Log.d(map.size() + " " + children.size());
            if (map.size() != children.size()) {
                return false;
            }
            for (FlowNode c : children) {
                Node child = map.get(c.className);
                if (child == null || !c.checkExactly(child)) {
                    return false;
                }
            }
            n.checkPass[0] = true;
            return true;
        }

        public boolean checkNormally(Node n, Graph graph) {
            if (n.checkPass[1]) {
                return true;
            }
            Map<String, Node> map = directChildrenOf(n);
            Log.e(map.size() + " " + children.size());
            for (FlowNode c : children) {
                Node child = map.remove(c.className);
                if (child == null && !c.allModified(graph)
                        || child != null && !c.checkNormally(child, graph)) {
                    return false;
                }
            }
            if (!map.values().stream()
                    .allMatch(FlowNode::neatInheritance)) {
                return false;
            }
            n.checkPass[1] = true;
            return true;
        }

        private boolean allModified(Graph graph) {
            Node t;
            return ((t = graph.get(className)) == null || t.status != Status.NOTCHANGED)
                    && children.stream().allMatch(c -> c.allModified(graph));
        }


        private static boolean neatInheritance(Node n) {
            return n.status != Status.NOTCHANGED
                    && directChildrenOf(n).values().stream().allMatch(FlowNode::neatInheritance);
        }

        private static Map<String, Node> directChildrenOf(Node n) {
            InterfaceNode t;
            return n instanceof ClassNode ?
                    ((ClassNode) n).children
                            .stream()
                            .collect(Collectors.toMap(v -> v.entity.name, v -> v)) :
                    Stream.concat((t = (InterfaceNode) n).children.stream(), t.implementedClasses.stream())
                            .collect(Collectors.toMap(v -> v.entity.name, v -> v));
        }
    }
}
