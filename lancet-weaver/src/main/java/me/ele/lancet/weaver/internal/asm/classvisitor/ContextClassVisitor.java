package me.ele.lancet.weaver.internal.asm.classvisitor;

import me.ele.lancet.weaver.internal.asm.LinkedClassVisitor;

/**
 * Created by gengwanpeng on 17/5/15.
 */
public class ContextClassVisitor extends LinkedClassVisitor {

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        getContext().name = name;
        getContext().superName = superName;
        super.visit(version, access, name, signature, superName, interfaces);
    }

}
