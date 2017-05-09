package me.ele.lancet.weaver.internal.asm.classvisitor;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import me.ele.lancet.weaver.internal.asm.MethodChain;
import me.ele.lancet.weaver.internal.util.AsmUtil;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.stream.Collectors;

import me.ele.lancet.weaver.internal.asm.ClassTransform;
import me.ele.lancet.weaver.internal.asm.LinkedClassVisitor;
import me.ele.lancet.weaver.internal.entity.ExecuteInfo;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.util.AopMethodAdjuster;
import me.ele.lancet.weaver.internal.util.TypeUtil;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class ExecuteClassVisitor extends LinkedClassVisitor {

    private Map<String, List<ExecuteInfo>> executeInfos;
    private List<ExecuteInfo> matched;


    public ExecuteClassVisitor(Map<String, List<ExecuteInfo>> executeInfos) {
        this.executeInfos = executeInfos;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        matched = executeInfos.get(className);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (matched != null) {
            List<ExecuteInfo> methodsMatched = new ArrayList<>(matched.size() >> 1);
            matched.removeIf(e -> {
                if (e.targetMethod.equals(name) && e.targetDesc.equals(desc)) {
                    if (((e.sourceMethod.access ^ access) & Opcodes.ACC_STATIC) != 0) {
                        throw new IllegalStateException(e.sourceClass + "." + e.sourceMethod.name + " should have the same static flag with "
                                + className + "." + name);
                    }
                    methodsMatched.add(e);
                    return true;
                }
                return false;
            });

            if (methodsMatched.size() > 0 && (access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) == 0) {
                Log.tag("transform").i("visit Insert method: " + className + "." + name + " " + desc);

                String staticDesc = TypeUtil.descToStatic(access, desc, className);
                ClassWriter cw = getClassCollector().getInnerClassWriter(ClassTransform.AID_INNER_CLASS_NAME);
                String owner = getClassCollector().getCanonicalName(ClassTransform.AID_INNER_CLASS_NAME);
                String newName = name + "$___twin___";
                int newAccess = (access & ~(Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC)) | Opcodes.ACC_PRIVATE;

                MethodChain chain = new MethodChain(cv);
                chain.headByAccess(access,className, newName, desc);
                methodsMatched.forEach(e -> {
                    String methodName = e.sourceClass.replace("/", "_") + "_" + e.sourceMethod.name;
                    chain.next(owner, Opcodes.ACC_STATIC, methodName, staticDesc, e.sourceMethod, cw);
                });
                chain.fake(className, access, name, desc, signature, exceptions);

                return super.visitMethod(newAccess, newName, desc, signature, exceptions);
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }


    private MethodNode createFakeMethod(int faccess, String fname, String fdesc, String fsignature, String[] fexceptions, int index, ExecuteInfo executeInfo, String originName, String staticDesc) {
        // all visitor will share the only one innerclass
        ClassWriter writer = getClassCollector().getInnerClassWriter(ClassTransform.AID_INNER_CLASS_NAME);

        String innerClassName = getClassCollector().getCanonicalName(ClassTransform.AID_INNER_CLASS_NAME);
        // every method in innerclass will add suffix with aop type & index
        String methodName = executeInfo.sourceClass.replace("/", "_") + "_" + fname;

        MethodVisitor proxyMethod = writer.visitMethod(Opcodes.ACC_STATIC, methodName, staticDesc, executeInfo.sourceMethod.signature, (String[]) executeInfo.sourceMethod.exceptions.toArray(new String[executeInfo.sourceMethod.exceptions.size()]));

        executeInfo.sourceMethod.accept(new MethodVisitor(Opcodes.ASM5, proxyMethod) {

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

                if (opcode == AopMethodAdjuster.OP_FLAG) {

                    opcode = (faccess & Opcodes.ACC_STATIC) == 0 ? Opcodes.INVOKESPECIAL : Opcodes.INVOKESTATIC;
                    owner = className;
                    name = originName;
                    desc = fdesc;

                    Type[] types = Type.getMethodType(staticDesc).getArgumentTypes();

                    int index = 0;
                    for (Type type : types) {
                        super.visitVarInsn(type.getOpcode(Opcodes.ILOAD), index);
                        index += type.getSize();
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc, false);
            }
            /**
             * override this method to delete 'this' var in method when origin method is nor static.
             * 'this' var is always at index 0 and length is 1.
             * So minus 1 when origin method is nor static.
             *//*
            @Override
            public void visitVarInsn(int opcode, int var) {
                if ((executeInfo.sourceMethod.access & Opcodes.ACC_STATIC) == 0) {
                    var--;
                }
                super.visitVarInsn(opcode, var);
            }

            *//**
             * edit the LocalVariable to delete 'this' var when origin method is nor static.
             *//*
            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                if ((executeInfo.sourceMethod.access & Opcodes.ACC_STATIC) == 0) {
                    if (name.equals("this")) {
                        return;
                    } else {
                        index--;
                    }
                }
                super.visitLocalVariable(name, desc, signature, start, end, index);
            }*/
        });

        // because the target method has renamed,so there will generate a fake method use origin method name.
        /*Type[] exceptions = null;
        if (fexceptions != null) {
            exceptions = Arrays.stream(fexceptions).map(Type::getType).collect(Collectors.toList()).toArray(new Type[0]);
        }*/

        MethodNode fakeMethod = new MethodNode(Opcodes.ASM5, faccess, fname, fdesc, fsignature, fexceptions);
        GeneratorAdapter adapter = new GeneratorAdapter(fakeMethod, faccess, fname, fdesc);

        int size = -4;
        if (!AsmUtil.isStatic(faccess)) {
            adapter.loadThis();
            size = 0;
        }
        adapter.loadArgs();
        adapter.invokeStatic(Type.getObjectType(innerClassName), new Method(methodName, staticDesc));
        adapter.returnValue();

        size += Type.getArgumentsAndReturnSizes(fdesc);

        int local = size >> 2;
        int stack = Math.max(local, size & 3);
        adapter.visitMaxs(stack, local);
        adapter.visitEnd();

        return fakeMethod;
    }

    @Override
    public void visitEnd() {
        if (matched != null && matched.size() > 0) {
            new ArrayList<>(matched).stream()
                    .collect(Collectors.groupingBy(e -> e.targetMethod)).forEach((k, v) -> {
                if (v.stream().anyMatch(e -> e.createSuper)) {
                    ExecuteInfo e = v.get(0);
                    MethodVisitor mv = visitMethod(e.sourceMethod.access, e.targetMethod, e.targetDesc, e.sourceMethod.signature,
                            (String[]) e.sourceMethod.exceptions.toArray(new String[0]));
                    GeneratorAdapter adapter = new GeneratorAdapter(mv, e.sourceMethod.access, e.targetMethod, e.targetDesc);
                    adapter.visitCode();
                    adapter.loadThis();
                    adapter.loadArgs();
                    adapter.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassName, e.targetMethod, e.targetDesc, false);
                    adapter.returnValue();
                    int sz = Type.getArgumentsAndReturnSizes(e.targetDesc);
                    adapter.visitMaxs(Math.max(sz >> 2, sz&3), sz >> 2);
                    adapter.visitEnd();
                }
            });
        }
        super.visitEnd();
    }


    //    private void createLostMethod() {
//        if (methodContainer!=null){
//            methodContainer.stream()
//                    .filter(m -> !m.used && m.createSuper)
//                    .forEach(this::createMethod);
//        }
//    }
//
//    private void createMethod(TargetMethodInfo m) {
//        MethodNode executeNode = m.executes.generate(0).sourceMethod;
//        String[] exceptions = new String[executeNode.exceptions.size()];
//        executeNode.exceptions.toArray(exceptions);
//
//        String desc = m.myDescriptor;
//
//        MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC, m.name, desc, null, exceptions);
//        GeneratorAdapter mg = new GeneratorAdapter(mv,
//                Opcodes.ACC_PUBLIC, m.name, desc);
//
//        // invoke super
//        mg.visitCode();
//        mg.loadThis();
//        mg.loadArgs();
//        mg.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassName, m.name, desc, false);
//        mg.returnValue();
//        int stack = Type.getArgumentsAndReturnSizes(desc) >> 2;
//        int local = (stack == 1 && (desc.endsWith("D") || desc.endsWith("J"))) ? 2 : stack;
//        mg.visitMaxs(stack, local);
//        mg.visitEnd();
//    }
//
//    private static class MethodContainer {
//        Map<String, TargetMethodInfo> map = new HashMap<>();
//
//        public MethodContainer addExecute(ExecuteInfo executeInfo) {
//            String key = executeInfo.targetMethod + " " + executeInfo.targetDesc;
//            TargetMethodInfo methodInfo = map.generate(key);
//            if (methodInfo == null) {
//                methodInfo = new TargetMethodInfo();
//                methodInfo.myDescriptor = executeInfo.targetDesc;
//                methodInfo.name = executeInfo.targetMethod;
//                map.put(key, methodInfo);
//            }
//            methodInfo.addExecute(executeInfo);
//            return this;
//        }
//
//        public MethodContainer combine(MethodContainer annotations) {
//            if (annotations != this) {
//                map.putAll(annotations.map);
//            }
//            return this;
//        }
//
//        public Stream<TargetMethodInfo> stream() {
//            return map.values().stream();
//        }
//
//        public TargetMethodInfo generate(String name, String desc) {
//            if (map.size() >= 0) {
//                return map.generate(name + " " + desc);
//            }
//            return null;
//        }
//    }
}
