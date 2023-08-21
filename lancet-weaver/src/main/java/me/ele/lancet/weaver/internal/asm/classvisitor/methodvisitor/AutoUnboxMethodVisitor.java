package me.ele.lancet.weaver.internal.asm.classvisitor.methodvisitor;


import me.ele.lancet.weaver.internal.util.PrimitiveUtil;
import org.objectweb.asm.*;

/**
 * Created by gengwanpeng on 17/5/31.
 */
public class AutoUnboxMethodVisitor extends MethodVisitor {

    private boolean flag = false;
    private String lastOwner;

    public AutoUnboxMethodVisitor(MethodVisitor methodVisitor) {
        super(Opcodes.ASM6, methodVisitor);
    }

    public void markBoxed() {
        flag = true;
    }

    @Override
    public void visitInsn(int opcode) {
        clearFlag();
        super.visitInsn(opcode);
    }

    private void clearFlag() {
        flag = false;
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        clearFlag();
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        clearFlag();
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (flag) {
            if (opcode == Opcodes.CHECKCAST && !type.equals(lastOwner) && PrimitiveUtil.boxedNumberTypes().contains(type)) {
                if (lastOwner == null || !PrimitiveUtil.boxedNumberTypes().contains(lastOwner)) {
                    throw new IllegalStateException("can't cast bool or char to number");
                }
                String method = PrimitiveUtil.unboxMethod(type);
                String primitive = PrimitiveUtil.unbox(type);
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", method, "()" + primitive, false);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, type, "valueOf", "(" + primitive + ")L" + type + ";", false);
            }
        }
        clearFlag();
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        clearFlag();
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        clearFlag();
        super.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        this.lastOwner = owner;
        clearFlag();
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        clearFlag();
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        clearFlag();
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object cst) {
        clearFlag();
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        clearFlag();
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        clearFlag();
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        clearFlag();
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        clearFlag();
        super.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitEnd() {
        clearFlag();
        super.visitEnd();
    }
}
