package me.ele.lancet.weaver.internal.asm;

import me.ele.lancet.weaver.internal.graph.Graph;
import org.objectweb.asm.ClassVisitor;

import java.util.BitSet;

/**
 * Created by gengwanpeng on 17/5/12.
 */
public class ClassContext {

    private final Graph graph;
    private final MethodChain chain;
    private final ClassVisitor tail;

    public String name;
    public String superName;

    public ClassContext(Graph graph, MethodChain chain, ClassVisitor tail) {
        this.graph = graph;
        this.chain = chain;
        this.tail = tail;
    }

    public ClassVisitor getTail() {
        return tail;
    }

    public Graph getGraph() {
        return graph;
    }

    public MethodChain getChain() {
        return chain;
    }

}
