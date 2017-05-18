package me.ele.lancet.weaver.internal.asm.classvisitor;

import me.ele.lancet.weaver.internal.asm.LinkedClassVisitor;
import org.objectweb.asm.*;

import java.util.Set;

/**
 * Created by gengwanpeng on 17/5/15.
 */
public class ContextClassVisitor extends LinkedClassVisitor {

    private final Set<String> excludes;

    public ContextClassVisitor(Set<String> excludes) {
        this.excludes = excludes;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        getContext().name = name;
        getContext().superName = superName;
        if (excludes.contains(name)) {
            this.cv = getContext().getTail(); // make delegate point to the tail, ignore middle transform
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }
}
