package me.ele.lancet.weaver.internal.asm.classvisitor.methodvisitor;

import me.ele.lancet.weaver.internal.entity.CallInfo;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.util.AopMethodAdjuster;
import me.ele.lancet.weaver.internal.util.AsmUtil;
import me.ele.lancet.weaver.internal.util.PrimitiveUtil;
import me.ele.lancet.weaver.internal.util.TypeUtil;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.TryCatchBlockSorter;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by gengwanpeng on 17/4/1.
 */
public class CallMethodVisitor extends //MethodNode {
        TryCatchBlockSorter {

    private final Map<String, List<CallInfo>> matchMap;
    private String targetClassName;


    public CallMethodVisitor(int api, int access, String name, String desc, String signature, String[] exceptions, MethodVisitor mv, Map<String, List<CallInfo>> matchMap, String targetClassName) {
        super(api, mv, access, name, desc, signature, exceptions);
        this.matchMap = matchMap;
        this.targetClassName = targetClassName;
        //this.mv = mv;
    }

    @Override
    public void visitEnd() {
        Log.tag("transform").i("start Call transform method: " + targetClassName + "." + name + " " + desc);
        try {
            transformCode();
            super.visitEnd();
        } catch (RuntimeException e) {
            throw new RuntimeException("transform: " + targetClassName + " " + name + " " + desc, e);
        }
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
    }

    private AbstractInsnNode addCall(List<CallInfo> infos, MethodInsnNode methodInsnNode) {
        Log.tag("addCall").d(methodInsnNode.owner + " " + methodInsnNode.name + " " + methodInsnNode.desc);
        Log.tag("added").d(
                infos.stream()
                        .map(i -> i.myClass + " " + i.myMethod + i.methodDescriptor)
                        .collect(Collectors.joining("\n")));
        int nowLocal = maxLocals;
        for (CallInfo callInfo : infos) {
            MethodNode clone = AsmUtil.clone(callInfo.node);
            // insert pop to local
            popToLocal(nowLocal, methodInsnNode);

            //try catch
            tryCatchBlocks.addAll(clone.tryCatchBlocks);
            // local var
            /*for(LocalVariableNode l : (List<LocalVariableNode>)clone.localVariables){
                l.index += nowLocal;
            }
            localVariables.addAll(clone.localVariables);*/

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
            nowLocal += clone.maxLocals;
            maxStack = Math.max(maxStack, clone.maxStack);
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
