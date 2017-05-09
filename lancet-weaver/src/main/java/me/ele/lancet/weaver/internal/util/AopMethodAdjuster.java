package me.ele.lancet.weaver.internal.util;

import me.ele.lancet.base.Origin;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

/**
 * Created by gengwanpeng on 17/3/28.
 */
public class AopMethodAdjuster {

    public static final int OP_FLAG = Integer.MAX_VALUE;

    private static final int VOID = 1;
    private static final int REFERENCE = 2;
    private static final int PRIMITIVE = 3;

    private final String sourceClass;
    private final MethodNode methodNode;


    private int type;
    //private char primitive;
    private String returnType;

    public AopMethodAdjuster(String sourceClass, MethodNode methodNode) {
        this.sourceClass = sourceClass;
        this.methodNode = methodNode;
        init();
    }

    private void init() {
        String desc = methodNode.desc;

        if (desc.endsWith("V")) {// void
            type = VOID;
        } else if (desc.endsWith(";") || desc.charAt(desc.lastIndexOf(')') + 1) == '[') { // object or array
            type = REFERENCE;
            returnType = desc.substring(desc.lastIndexOf(')') + 1);

            if (returnType.charAt(0) != '[' && returnType.endsWith(";")) { //convert to internal name
                returnType = returnType.substring(1, returnType.length() - 1);
            }
        } else { //primitive
            type = PRIMITIVE;
            char primitive = desc.charAt(desc.lastIndexOf(')') + 1);
            returnType = PrimitiveUtil.box(primitive);
        }

        // update max locals
        int size = Type.getArgumentsAndReturnSizes(desc);
        if (AsmUtil.isStatic(methodNode.access)) {
            size -= 4;
        }
        size = Math.max(size & 3, size >> 2);
        methodNode.maxStack = Math.max(size, methodNode.maxStack);
    }

    public void adjust() {
        int step = 0;
        AbstractInsnNode element = methodNode.instructions.getFirst();
        while (element != null) {
            if (step == 0 && element instanceof MethodInsnNode) { //MethodInsnNode
                MethodInsnNode methodInsnNode = (MethodInsnNode) element;
                if (methodInsnNode.owner.equals(Origin.CLASS_NAME)
                        && methodInsnNode.name.startsWith("call")) { //start with Origin's function
                    element = replaceToCallTargetFunction(methodInsnNode);
                    if (type != VOID) {
                        step++;
                    }
                } else if (methodInsnNode.owner.equals(Origin.CLASS_NAME)
                        && methodInsnNode.name.equals("loadThis")) {
                    element = replaceToAload(methodInsnNode);
                } else {
                    element = element.getNext();
                }
            } else if (step == 1) {
                step++;
                //must be checkcast instruction or return object
                if (!returnType.equals("java/lang/Object")) {
                    element = checkCast(element);
                } else {
                    element = element.getNext();
                }
                if (type != PRIMITIVE) {
                    step = 0;
                }
            } else if (step == 2) {
                step = 0;
                // must be unbox method
                element = checkUnbox(element);
            } else {
                if (step != 0) {
                    throw new IllegalStateException("called function in Origin don't match your function signature.");
                }
                element = element.getNext();
            }
        }
    }

    private AbstractInsnNode replaceToAload(MethodInsnNode methodInsnNode) {
        if(AsmUtil.isStatic(methodNode.access)){
            illegalState("Static method shouldn't call loadThis().");
        }

        VarInsnNode varInsnNode = new VarInsnNode(Opcodes.ALOAD, 0);
        methodNode.instructions.set(methodInsnNode, varInsnNode);
        return varInsnNode.getNext();
    }

    private AbstractInsnNode replaceToCallTargetFunction(MethodInsnNode insnNode) {
        boolean has = !insnNode.name.startsWith("callVoid");
        boolean hasInDesc = !methodNode.desc.endsWith("V");
        if (has != hasInDesc) {
            illegalState("Called function " + insnNode.owner + "." + insnNode.name + "is illegal.");
        }

        MethodInsnNode placeHolder = new MethodInsnNode(OP_FLAG, null, null, null, false);
        //VarInsnNode varInsnNode = new VarInsnNode(Opcodes.ASTORE, 5);
        methodNode.instructions.set(insnNode, placeHolder);
        return placeHolder.getNext();
    }

    private AbstractInsnNode checkCast(AbstractInsnNode insnNode) {
        if (!(insnNode instanceof TypeInsnNode)) {
            illegalState("Returned Object type should be cast to origin type immediately.");
        }
        TypeInsnNode typeInsnNode = (TypeInsnNode) insnNode;
        if (typeInsnNode.getOpcode() != Opcodes.CHECKCAST) {
            illegalState("Returned Object type should be cast to origin type immediately.");
        }
        if (!typeInsnNode.desc.equals(returnType)) {
            illegalState("Casted type is expected to be " + returnType + " , but is " + typeInsnNode.desc);
        }

        // asm bug: sometimes checkcast will appear one more
        // but it doesn't matter (:

        /*AbstractInsnNode next;
        while (true) {
            next = insnNode.getNext();
            methodNode.instructions.remove(insnNode);
            if (next instanceof TypeInsnNode) {
                typeInsnNode = (TypeInsnNode) next;
                if (typeInsnNode.getOpcode() == Opcodes.CHECKCAST) {
                    insnNode = typeInsnNode;
                    continue;
                }
            }
            break;
        }
        return next;*/
        insnNode = typeInsnNode.getNext();
        methodNode.instructions.remove(typeInsnNode);
        return insnNode;
    }

    private AbstractInsnNode checkUnbox(AbstractInsnNode insnNode) {
        if (!(insnNode instanceof MethodInsnNode)) {
            illegalState("Please don't unbox by your self.");
        }
        MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
        if (!methodInsnNode.owner.equals(returnType)) {
            illegalState("Please don't unbox by your self.");
        }
        if (!methodInsnNode.name.equals(PrimitiveUtil.unboxMethod(returnType))) {
            illegalState("Please don't unbox by your self.");
        }
        insnNode = methodInsnNode.getNext();
        methodNode.instructions.remove(methodInsnNode);
        return insnNode;
    }


    private void illegalState(String msg) {
        throw new IllegalStateException("error in " + sourceClass + "." + methodNode.name + "." + msg);
    }
}
