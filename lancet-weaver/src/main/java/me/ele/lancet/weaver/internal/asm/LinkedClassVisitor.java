package me.ele.lancet.weaver.internal.asm;

import me.ele.lancet.weaver.internal.graph.Graph;
import me.ele.lancet.weaver.internal.util.Bitset;

import org.objectweb.asm.*;

/**
 * Created by Jude on 2017/4/25.
 */

public class LinkedClassVisitor extends ClassVisitor {


    private ClassContext context;
    private ClassCollector mClassCollector;


    public LinkedClassVisitor() {
        super(Opcodes.ASM5);
    }

    public void setContext(ClassContext context) {
        this.context = context;
    }

    void setClassCollector(ClassCollector classCollector) {
        this.mClassCollector = classCollector;
    }

    public ClassContext getContext() {
        return context;
    }

    protected ClassCollector getClassCollector() {
        return mClassCollector;
    }

    public void setNextClassVisitor(ClassVisitor classVisitor) {
        cv = classVisitor;
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        return null;
    }
}
