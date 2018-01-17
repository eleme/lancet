package me.ele.lancet.weaver.internal.asm.classvisitor.methodvisitor;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.ele.lancet.weaver.internal.entity.TryCatchInfo;


/**
 * Created by gengwanpeng on 17/3/31.
 */
public class TryCatchMethodVisitor extends MethodVisitor {

    private Set<Label> handlers = new HashSet<>();
    private final List<TryCatchInfo> matches;

    public TryCatchMethodVisitor(int api, MethodVisitor mv, List<TryCatchInfo> matches) {
        super(api, mv);
        this.matches = matches;
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        handlers.add(handler);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        if (label != null && handlers.contains(label)) {
            for (TryCatchInfo info : matches) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, info.myClass, info.myMethod, info.methodDescriptor, false);
            }
        }
    }
}
