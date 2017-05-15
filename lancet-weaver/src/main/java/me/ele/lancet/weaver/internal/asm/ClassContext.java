package me.ele.lancet.weaver.internal.asm;

import me.ele.lancet.weaver.internal.graph.Graph;

import java.util.BitSet;

/**
 * Created by gengwanpeng on 17/5/12.
 */
public class ClassContext {

    private final Graph graph;
    private final MethodChain chain;

    public String name;
    public String superName;

    public ClassContext(Graph graph, MethodChain chain) {
        this.graph = graph;
        this.chain = chain;
    }

    public Graph getGraph() {
        return graph;
    }

    public MethodChain getChain() {
        return chain;
    }

}
