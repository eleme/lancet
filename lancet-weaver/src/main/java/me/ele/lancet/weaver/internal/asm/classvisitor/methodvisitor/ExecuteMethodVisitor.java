package me.ele.lancet.weaver.internal.asm.classvisitor.methodvisitor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;

import me.ele.lancet.weaver.internal.entity.ExecuteInfo;
import me.ele.lancet.weaver.internal.entity.TargetMethodInfo;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.util.AopMethodAdjuster;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class ExecuteMethodVisitor extends MethodNode {

    private MethodVisitor delegate;
    private String targetClassName;
    private TargetMethodInfo targetMethodInfo;

    private int paramLocals;

    public ExecuteMethodVisitor(int api, int access, String name, String desc, String signature, String[] exceptions, MethodVisitor delegate) {
        super(api, access, name, desc, signature, exceptions);
        this.delegate = delegate;
        paramLocals = (Type.getArgumentsAndReturnSizes(desc) >> 2) - 1;
        if ((access & Opcodes.ACC_STATIC) == 0) {
            paramLocals++;
        }
    }

    public void setAopInfo(String targetClassName, TargetMethodInfo targetMethodInfo) {
        this.targetClassName = targetClassName;
        this.targetMethodInfo = targetMethodInfo;
    }

    @Override
    public void visitEnd() {
        transform();
        accept(delegate);
    }

    public void transform() {
        int fromIndex = this.maxLocals;
        for (ExecuteInfo executeInfo : targetMethodInfo.executes) {
            Log.i("executeInfo: "+executeInfo);
            fromIndex = transformEachExecute(fromIndex, executeInfo);
        }
        maxLocals = fromIndex;
    }

    private int transformEachExecute(int fromIndex, ExecuteInfo executeInfo) {
        int addition = fromIndex - paramLocals;

        // transform return to jump
        removeReturnInsn();

        MethodNode clone = new MethodNode();
        executeInfo.node.accept(clone);
        InsnList insnList = clone.instructions;
        boolean callOrigin = findCallOrigin(insnList);

        if (callOrigin) {
            if (tryCatchBlocks == null) tryCatchBlocks = new ArrayList();
            if (clone.tryCatchBlocks != null) tryCatchBlocks.addAll(clone.tryCatchBlocks);
        }

        AbstractInsnNode element = insnList.getFirst();
        while (element != null) {
            if (element instanceof VarInsnNode) {
                VarInsnNode varInsnNode = (VarInsnNode) element;
                if (varInsnNode.getOpcode() != Opcodes.RET) {
                    if (varInsnNode.var >= paramLocals && callOrigin) {
                        varInsnNode.var += addition;
                    }
                }
            } else if (element instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) element;
                if (methodInsnNode.getOpcode() == AopMethodAdjuster.OP_FLAG) {
                    insnList.insertBefore(methodInsnNode, instructions);
                    element = methodInsnNode.getPrevious();
                    insnList.remove(methodInsnNode);
                    if (element == null) {
                        element = insnList.getFirst();
                    }
                }
            }
            element = element.getNext();
        }

        instructions = insnList;
        int newIndex = executeInfo.node.maxLocals;
        if (callOrigin) {
            newIndex = newIndex - paramLocals + fromIndex;
        }
        return newIndex;
    }

    private boolean findCallOrigin(InsnList insnList) {
        boolean callOrigin = false;
        for (int i = 0; i < insnList.size(); i++) {
            AbstractInsnNode abstractInsnNode = insnList.get(i);
            if (abstractInsnNode instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
                if (methodInsnNode.getOpcode() == AopMethodAdjuster.OP_FLAG) {
                    callOrigin = true;
                    break;
                }
            }
        }
        return callOrigin;
    }

    private void removeReturnInsn() {
        AbstractInsnNode element = instructions.getFirst();
        LabelNode labelNode = new LabelNode();
        while (element != null) {
            if (element instanceof InsnNode) {
                InsnNode insnNode = (InsnNode) element;
                if (Opcodes.IRETURN <= insnNode.getOpcode()
                        && insnNode.getOpcode() <= Opcodes.RETURN) {
                    JumpInsnNode jumpInsnNode = new JumpInsnNode(Opcodes.GOTO, labelNode);
                    instructions.set(insnNode, jumpInsnNode);
                    element = jumpInsnNode;
                }
            }
            element = element.getNext();
        }
        instructions.add(labelNode);
    }
}