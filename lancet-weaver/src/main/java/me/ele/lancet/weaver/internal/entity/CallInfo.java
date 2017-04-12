package me.ele.lancet.weaver.internal.entity;

import com.google.common.base.Strings;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.regex.Pattern;

import me.ele.lancet.weaver.internal.util.AopMethodAdjuster;
import me.ele.lancet.weaver.internal.util.TypeUtil;


/**
 * Created by gengwanpeng on 17/3/27.
 */
public class CallInfo {

    public boolean isStatic;
    public String regex;

    public String targetClass;
    public String targetMethod;
    public String methodDescriptor;
    public String myClass;
    public String myMethod;
    public MethodNode node;

    private Pattern pattern;
    private String targetDesc;

    public CallInfo(boolean isStatic, String regex, String targetClass, String targetMethod, String methodDescriptor, String myClass, String myMethod, MethodNode node) {
        this.isStatic = isStatic;
        this.regex = regex;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.methodDescriptor = methodDescriptor;
        this.myClass = myClass;
        this.myMethod = myMethod;
        this.node = node;

        if (!Strings.isNullOrEmpty(regex)) {
            this.pattern = Pattern.compile(regex);
        }
    }

    public String targetDesc() {
        return isStatic ? methodDescriptor : (targetDesc == null ? targetDesc = TypeUtil.removeFirstParam(methodDescriptor) : targetDesc);
    }

    public boolean match(String className) {
        return pattern == null || pattern.matcher(className).matches();
    }

    public void transformSelf() {
        AbstractInsnNode element = node.instructions.getFirst();
        LabelNode labelNode = new LabelNode();
        while (element != null) {
            if (element instanceof InsnNode) {
                InsnNode insnNode = (InsnNode) element;
                if (Opcodes.IRETURN <= insnNode.getOpcode()
                        && insnNode.getOpcode() <= Opcodes.RETURN) {
                    JumpInsnNode jumpInsnNode = new JumpInsnNode(Opcodes.GOTO, labelNode);
                    node.instructions.set(insnNode, jumpInsnNode);
                    element = jumpInsnNode;
                }
            } else if (element instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) element;
                if (methodInsnNode.getOpcode() == AopMethodAdjuster.OP_FLAG) {
                    node.instructions.insertBefore(methodInsnNode, loadLocalToStack(isStatic, targetMethod, targetDesc()));
                    methodInsnNode.owner = targetClass.replace('.', '/');
                    methodInsnNode.name = targetMethod;
                    methodInsnNode.desc = targetDesc();
                }
            }
            element = element.getNext();
        }
        node.instructions.add(labelNode);
    }

    private static InsnList loadLocalToStack(boolean isStatic, String name, String desc) {
        MethodNode methodNode = new MethodNode();
        GeneratorAdapter generatorAdapter = new GeneratorAdapter(methodNode, isStatic ? Opcodes.ACC_STATIC : Opcodes.ACC_PUBLIC, name, desc);
        if (!isStatic) {
            generatorAdapter.loadThis();
        }
        generatorAdapter.loadArgs();
        return methodNode.instructions;
    }
}
