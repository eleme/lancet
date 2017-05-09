package me.ele.lancet.weaver.internal.asm;

import me.ele.lancet.weaver.internal.util.AopMethodAdjuster;
import me.ele.lancet.weaver.internal.util.AsmUtil;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * Created by gengwanpeng on 17/5/9.
 */
public class MethodChain {


    private final ClassVisitor base;


    private int opcode;
    private String owner;
    private String name;
    private String desc;


    public MethodChain(ClassVisitor base) {
        this.base = base;
    }

    public int getHeadOpcode() {
        return opcode;
    }

    public String getHeadOwner() {
        return owner;
    }

    public String getHeadName() {
        return name;
    }

    public String getHeadDesc() {
        return desc;
    }

    public void head(int opcode, String owner, String name, String desc) {
        this.opcode = opcode;
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    public void headByAccess(int access, String owner, String name, String desc) {
        head(AsmUtil.isStatic(access) ? Opcodes.INVOKESTATIC : Opcodes.INVOKESPECIAL, owner, name, desc);
    }


    public void next(String className, int access, String name, String desc, MethodNode node, ClassVisitor cv) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, node.signature, (String[]) node.exceptions.toArray(new String[0]));
        node.accept(new MethodVisitor(Opcodes.ASM5, mv) {

            int paramSize;

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                if (opcode == AopMethodAdjuster.OP_FLAG) {
                    paramSize = invokePrevMethod(mv);
                } else {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                maxStack = Math.max(paramSize, maxStack);
                maxStack = Math.max(maxStack, Type.getReturnType(MethodChain.this.desc).getSize());
                super.visitMaxs(maxStack, maxLocals);
            }
        });

        headByAccess(access, className, name, desc);
    }

    public void fake(String className, int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = base.visitMethod(access, name, desc, signature, exceptions);
        mv.visitCode();

        Type ret = Type.getReturnType(this.desc);
        int index = invokePrevMethod(mv);

        mv.visitInsn(ret.getOpcode(Opcodes.IRETURN));
        mv.visitMaxs(Math.max(index, ret.getSize()), index);
        mv.visitEnd();
        headByAccess(access, className, name, desc);
    }

    private int invokePrevMethod(MethodVisitor mv) {
        Type[] params = Type.getArgumentTypes(this.desc);
        int index = 0;
        if (this.opcode != Opcodes.INVOKESTATIC) {
            index = 1;
            mv.visitVarInsn(Opcodes.ALOAD, 0);
        }
        for (Type t : params) {
            mv.visitVarInsn(t.getOpcode(Opcodes.ILOAD), index);
            index += t.getSize();
        }
        mv.visitMethodInsn(opcode, owner, name, desc, false);
        return index;
    }
}
