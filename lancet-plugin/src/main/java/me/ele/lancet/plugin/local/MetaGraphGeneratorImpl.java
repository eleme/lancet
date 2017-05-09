package me.ele.lancet.plugin.local;

import me.ele.lancet.weaver.internal.graph.ClassNode;
import me.ele.lancet.weaver.internal.graph.MetaGraphGenerator;
import me.ele.lancet.weaver.internal.graph.InterfaceNode;
import me.ele.lancet.weaver.internal.graph.Node;
import org.objectweb.asm.Opcodes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by gengwanpeng on 17/4/26.
 */
public class MetaGraphGeneratorImpl implements MetaGraphGenerator {

    private Map<String, Node> nodeMap = new ConcurrentHashMap<>(512);
    private AtomicInteger lock = new AtomicInteger(0);

    public MetaGraphGeneratorImpl() {
    }

    // thread safe
    public void add(int access, String className, String superClassName, String[] interfaces) {
        Node current = getOrPutEmpty((access & Opcodes.ACC_INTERFACE) != 0, className);
        ClassNode superNode = null;
        List<InterfaceNode> nodeList = Collections.emptyList();
        if (superClassName != null) {
            superNode = (ClassNode) getOrPutEmpty(false, superClassName);
            if (interfaces != null && interfaces.length > 0) {
                nodeList = Arrays.stream(interfaces).map(i -> (InterfaceNode)getOrPutEmpty(true, i)).collect(Collectors.toList());
            }
        }
        current.access = access;
        current.parent = superNode;
        current.interfaces = nodeList;
    }

    public void remove(String className) {
        Node node = nodeMap.get(className);
        if (node != null) {
            node.parent = null;
            node.interfaces = Collections.emptyList();
        }
    }

    private Node getOrPutEmpty(boolean isInterface, String className) {
        return nodeMap.computeIfAbsent(className, n -> isInterface ?
                new InterfaceNode(n) :
                new ClassNode(n));
        /*Node node = null;
        while (true) {
            if (lock.getAndIncrement() == 0) {
                node = nodeMap.computeIfAbsent(className, n -> isInterface ?
                        new InterfaceNode(n) :
                        new ClassNode(n));
            }
            lock.decrementAndGet();
            if (node != null) {
                return node;
            }
        }*/
    }


    List<Metas.NodeLike> toLocalNodes() {
        return nodeMap.values().stream().filter(it -> it.parent != null).map(it -> {
            Metas.NodeLike nodeLike = new Metas.NodeLike();
            nodeLike.access = it.access;
            nodeLike.name = it.className;
            nodeLike.superName = it.parent.className;
            nodeLike.interfaces = it.interfaces.stream().map(i -> i.className).toArray(String[]::new);
            return nodeLike;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Node> generate() {
        return nodeMap;
    }
}
