package me.ele.lancet.weaver.internal.asm.classvisitor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Set;

import me.ele.lancet.weaver.internal.asm.LinkedClassVisitor;
import me.ele.lancet.weaver.internal.util.TypeUtil;

/**
 * Created by gengwanpeng on 17/5/15.
 */
public class HookClassVisitor extends LinkedClassVisitor {

    private final Set<String> excludes;
    private boolean matched;

    public HookClassVisitor(Set<String> excludes) {
        this.excludes = excludes;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        getContext().name = name;
        getContext().superName = superName;
        if (excludes.contains(name)) {
            matched = true;
            this.cv = getContext().getTail(); // make delegate point to the tail, ignore middle transform
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (matched){
            return super.visitMethod(TypeUtil.resetAccessScope(access,Opcodes.ACC_PUBLIC), name, desc, signature, exceptions);
        }else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }
}
