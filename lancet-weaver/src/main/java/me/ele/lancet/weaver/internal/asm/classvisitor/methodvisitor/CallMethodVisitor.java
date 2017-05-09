package me.ele.lancet.weaver.internal.asm.classvisitor.methodvisitor;

import me.ele.lancet.weaver.internal.asm.MethodChain;
import me.ele.lancet.weaver.internal.util.AsmUtil;
import me.ele.lancet.weaver.internal.util.TypeUtil;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;
import java.util.Map;

import me.ele.lancet.weaver.internal.asm.ClassCollector;
import me.ele.lancet.weaver.internal.asm.ClassTransform;
import me.ele.lancet.weaver.internal.entity.CallInfo;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.util.AopMethodAdjuster;

/**
 * Created by Jude on 17/4/26.
 */
public class CallMethodVisitor extends MethodVisitor {

    private final Map<String, List<CallInfo>> matchMap;
    private final String className;
    private final String name;
    private final ClassCollector classCollector;
    private final MethodChain chain;

    public CallMethodVisitor(MethodChain chain, MethodVisitor mv, Map<String, List<CallInfo>> matchMap, String className, String name, ClassCollector classCollector) {
        super(Opcodes.ASM5, mv);
        this.chain = chain;
        this.matchMap = matchMap;
        this.className = className;
        this.name = name;
        this.classCollector = classCollector;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        List<CallInfo> infos = matchMap.get(owner + " " + name + " " + desc);

        if (infos != null && infos.size() > 0) {

            String staticDesc = TypeUtil.descToStatic(opcode == Opcodes.INVOKESTATIC ? Opcodes.ACC_STATIC : 0, desc, owner);
            // begin hook this code.
            chain.head(opcode, owner, name, desc);

            String artificialClassname = classCollector.getCanonicalName(ClassTransform.AID_INNER_CLASS_NAME);
            ClassWriter cw = classCollector.getInnerClassWriter(ClassTransform.AID_INNER_CLASS_NAME);

            Log.tag("transform").i("start weave Call method " + " for " + owner + "." + name + desc +
                    " in " + className + "." + this.name);

            infos.forEach(c -> {
                if (AsmUtil.isStatic(c.sourceMethod.access) != (opcode == Opcodes.INVOKESTATIC)) {
                    throw new IllegalStateException(c.sourceClass + "." + c.sourceMethod.name + " should have the same " +
                            "static flag with " + owner + "." + name);
                }
                Log.tag("transform").i(
                        " from " + c.sourceClass + "." + c.sourceMethod.name);

                String methodName = c.sourceClass.replace("/", "_") + "_" + c.sourceMethod.name;
                chain.next(artificialClassname, Opcodes.ACC_STATIC, methodName, staticDesc, c.sourceMethod, cw);
            });

            super.visitMethodInsn(chain.getHeadOpcode(), chain.getHeadOwner(), chain.getHeadName(), chain.getHeadDesc(), false);
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }


    private void proxy(List<CallInfo> infos, MethodInsnNode methodInsnNode) {
        String staticDesc = methodInsnNode.desc;
        if (methodInsnNode.getOpcode() != Opcodes.INVOKESTATIC) {
            staticDesc = "(L" + methodInsnNode.owner + ";" + staticDesc.substring(1);
        }
        for (int i = 0; i < infos.size(); i++) {
            Log.tag("transform").i("start weave Call method: " + className + ".?" + " for " + infos.get(i).targetClass + "." + infos.get(i).targetMethod);
            proxyOne(infos.get(i), methodInsnNode, i, staticDesc);
        }
    }

    /**
     * This method will handle a CallInfo.
     * first, generate a innerClass,just like this.
     * copy the method from Hook class to the innerClass:
     * <p>
     * private static class _lancet {
     * public static void com_sample_hook_call_Hook2_putCoffee(Cup cup, String coffee) {
     * System.out.println("replace " + coffee + " with Cappuccino before add to cup");
     * coffee = "Cappuccino";
     * cup.putCoffee(coffee);
     * }
     * }
     * <p>
     * and then,change the invoke code to invoke the innerClass method,like this:
     * <p>
     * public Cup brew(Cup cup) {
     * CoffeeMaker._lancet.com.sample.hook.call.Hook2_putCoffee(cup, this.coffeeBox.getLatte());
     * return cup;
     * }
     *
     * @param info           hook info entry
     * @param methodInsnNode the code to invoke target method
     * @param index          index of CallInfo in CallInfo List
     */
    private void proxyOne(CallInfo info, MethodInsnNode methodInsnNode, int index, String staticDesc) {
        // all visitor will share the only one innerclass
        ClassWriter writer = classCollector.getInnerClassWriter(ClassTransform.AID_INNER_CLASS_NAME);

        String innerClassName = classCollector.getCanonicalName(ClassTransform.AID_INNER_CLASS_NAME);
        // every method in innerclass will add source class name as prefix
        String methodName = info.sourceClass.replace("/", "_") + "_" + info.sourceMethod.name;

        MethodVisitor proxyMethod = writer.visitMethod(Opcodes.ACC_STATIC, methodName, staticDesc, info.sourceMethod.signature, ((List<String>) info.sourceMethod.exceptions).toArray(new String[0]));
        // write origin method code to proxyMethod, and change the Origin.call() to invoke target method.
        info.sourceMethod.accept(new MethodVisitor(Opcodes.ASM5, proxyMethod) {

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

                if (opcode == AopMethodAdjuster.OP_FLAG) {
                    // invoke target method
                    opcode = methodInsnNode.getOpcode();
                    owner = methodInsnNode.owner;
                    name = methodInsnNode.name;
                    desc = methodInsnNode.desc;
                    itf = methodInsnNode.itf;

                    // load all arguments.
                    Type[] types = Type.getMethodType(staticDesc).getArgumentTypes();
                    int index = 0;
                    for (Type type : types) {
                        super.visitVarInsn(type.getOpcode(Opcodes.ILOAD), index);
                        index += type.getSize();
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                return null;
            }

            /**
             * override this method to delete 'this' var in method when origin method is nor static.
             * 'this' var is always at index 0 and length is 1.
             * So minus 1 when origin method is nor static.
             *//*
            @Override
            public void visitVarInsn(int opcode, int var) {
                if ((info.sourceMethod.access & Opcodes.ACC_STATIC) == 0) {
                    var--;
                }
                super.visitVarInsn(opcode, var);
            }

            *//**
             * edit the LocalVariable to delete 'this' var when origin method is nor static.
             *//*
            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                if ((info.sourceMethod.access & Opcodes.ACC_STATIC) == 0) {
                    if (name.equals("this")) {
                        return;
                    } else {
                        index--;
                    }
                }
                super.visitLocalVariable(name, desc, signature, start, end, index);
            }*/
        });

        // redirection the invoke code to proxy method in innerclass.
        methodInsnNode.setOpcode(Opcodes.INVOKESTATIC);
        methodInsnNode.owner = innerClassName;
        methodInsnNode.name = methodName;
        methodInsnNode.desc = staticDesc;
        methodInsnNode.itf = false;
    }

}
