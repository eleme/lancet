package me.ele.lancet.plugin.local;

import me.ele.lancet.weaver.internal.graph.*;
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

    public MetaGraphGeneratorImpl() {
    }

    // thread safe
    public void add(ClassEntity entity) {
        Node current = getOrPutEmpty((entity.access & Opcodes.ACC_INTERFACE) != 0, entity.name);

        ClassNode superNode = null;
        List<InterfaceNode> interfaceNodes = Collections.emptyList();
        if (entity.superName != null) {
            superNode = (ClassNode) getOrPutEmpty(false, entity.superName);
        }
        if (entity.interfaces.size() > 0) {
            interfaceNodes = entity.interfaces.stream().map(i -> (InterfaceNode) getOrPutEmpty(true, i)).collect(Collectors.toList());
        }

        current.entity = entity;
        current.parent = superNode;
        current.interfaces = interfaceNodes;
    }

    public void remove(String className) {
        nodeMap.remove(className);
    }

    private Node getOrPutEmpty(boolean isInterface, String className) {
        return nodeMap.computeIfAbsent(className, n -> isInterface ?
                new InterfaceNode(n) :
                new ClassNode(n));
    }


    List<ClassEntity> toLocalNodes() {
        return nodeMap.values().stream().map(it -> it.entity).collect(Collectors.toList());
    }

    @Override
    public Map<String, Node> generate() {
        return nodeMap;
    }
}
