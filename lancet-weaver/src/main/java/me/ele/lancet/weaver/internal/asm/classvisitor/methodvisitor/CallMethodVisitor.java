package me.ele.lancet.weaver.internal.asm.classvisitor.methodvisitor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.ele.lancet.weaver.internal.entity.CallInfo;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.util.AopMethodAdjuster;
import me.ele.lancet.weaver.internal.util.PrimitiveUtil;
import me.ele.lancet.weaver.internal.util.TypeUtil;

/**
 * Created by gengwanpeng on 17/4/1.
 */
public class CallMethodVisitor extends MethodNode {

    private final Map<String, List<CallInfo>> matchMap;
    private String targetClassName;


    public CallMethodVisitor(int api, int access, String name, String desc, String signature, String[] exceptions, MethodVisitor mv, Map<String, List<CallInfo>> matchMap,String targetClassName) {
        super(api, access, name, desc, signature, exceptions);
        this.matchMap = matchMap;
        this.targetClassName = targetClassName;
        this.mv = mv;

    }

    @Override
    public void visitEnd() {
        Log.tag("transform").i("start Call transform method: "+targetClassName+"."+name+" "+desc);
        transformCode();
        super.visitEnd();
    }

    private void transformCode() {
        AbstractInsnNode element = instructions.getFirst();
        while (element != null) {
            if (element instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) element;
                List<CallInfo> infos = matchMap.get(methodInsnNode.owner + " " + methodInsnNode.name + " " + methodInsnNode.desc);
                if (infos != null) {
                    element = addCall(infos, methodInsnNode);
                }
            }
            if (element == null) {
                element = instructions.getFirst();
            } else {
                element = element.getNext();
            }
        }
        accept(mv);
    }

    private AbstractInsnNode addCall(List<CallInfo> infos, MethodInsnNode methodInsnNode) {
        int nowLocal = maxLocals;
        for (CallInfo callInfo : infos) {
            MethodNode node = callInfo.node;
            MethodNode clone = new MethodNode();
            node.instructions.accept(clone);
            // insert pop to local
            popToLocal(nowLocal, methodInsnNode);

            // insert aop codes
            for (AbstractInsnNode e : clone.instructions.toArray()) {
                if (e instanceof VarInsnNode) {
                    VarInsnNode varInsnNode = (VarInsnNode) e;
                    if (varInsnNode.getOpcode() != Opcodes.RET) {
                        varInsnNode.var += nowLocal;
                    }
                } else if (e instanceof MethodInsnNode) {
                    MethodInsnNode m = (MethodInsnNode) e;
                    if (m.getOpcode() == AopMethodAdjuster.OP_FLAG) {
                        m.setOpcode(methodInsnNode.getOpcode());
                        m.itf = m.getOpcode() == Opcodes.INVOKEINTERFACE;
                    }
                }
                instructions.insertBefore(methodInsnNode, e);
            }
            nowLocal += node.maxLocals;
        }
        maxLocals = nowLocal;
        AbstractInsnNode tempNode = methodInsnNode.getPrevious();
        instructions.remove(methodInsnNode);
        return tempNode;
    }

    private void popToLocal(int nowLocal, MethodInsnNode methodInsnNode) {
        InsnCollector insns = new InsnCollector();
        if (methodInsnNode.getOpcode() != Opcodes.INVOKESTATIC) {
            nowLocal = insns.storeRef(nowLocal);
        }
        int index = 1;
        String desc = methodInsnNode.desc;
        while (true) {
            char c = desc.charAt(index);
            switch (c) {
                case ')':
                    insns.reverseAddTo(instructions, methodInsnNode);
                    return;
                case '[':
                    index = TypeUtil.parseArray(index, desc);
                    nowLocal = insns.storeRef(nowLocal);
                    break;
                case 'L':
                    index = TypeUtil.parseObject(index, desc);
                    nowLocal = insns.storeRef(nowLocal);
                    break;
                case 'D':
                    nowLocal = insns.storeDouble(nowLocal);
                    break;
                case 'J':
                    nowLocal = insns.storeLong(nowLocal);
                    break;
                case 'F':
                    nowLocal = insns.storeFloat(nowLocal);
                    break;
                default:
                    nowLocal = insns.storeInt(nowLocal);
                    if (!PrimitiveUtil.primitives().contains(c)) {
                        throw new IllegalArgumentException("illegal type: " + c);
                    }
            }
            index++;
        }
    }

    private class InsnCollector {

        List<AbstractInsnNode> list = new ArrayList<>(4);

        private int storeRef(int local) {
            VarInsnNode varInsnNode = new VarInsnNode(Opcodes.ASTORE, local);
            list.add(varInsnNode);
            return local + 1;
        }

        private int storeDouble(int local) {
            VarInsnNode varInsnNode = new VarInsnNode(Opcodes.DSTORE, local);
            list.add(varInsnNode);
            return local + 2;
        }

        private int storeInt(int local) {
            VarInsnNode varInsnNode = new VarInsnNode(Opcodes.ISTORE, local);
            list.add(varInsnNode);
            return local + 1;
        }

        private int storeFloat(int local) {
            VarInsnNode varInsnNode = new VarInsnNode(Opcodes.FSTORE, local);
            list.add(varInsnNode);
            return local + 1;
        }

        private int storeLong(int local) {
            VarInsnNode varInsnNode = new VarInsnNode(Opcodes.LSTORE, local);
            list.add(varInsnNode);
            return local + 2;
        }

        private void reverseAddTo(InsnList insnList, AbstractInsnNode location) {
            for (int i = list.size() - 1; i >= 0; i--) {
                insnList.insertBefore(location, list.get(i));
            }
        }
    }
}
