package me.ele.lancet.weaver.internal.asm.classvisitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import me.ele.lancet.weaver.internal.asm.classvisitor.methodvisitor.ExecuteMethodVisitor;
import me.ele.lancet.weaver.internal.entity.ExecuteInfo;
import me.ele.lancet.weaver.internal.entity.TargetMethodInfo;
import me.ele.lancet.weaver.internal.entity.TotalInfo;


/**
 * Created by gengwanpeng on 17/3/27.
 */
public class ExecuteClassVisitor extends ClassVisitor {

    private List<ExecuteInfo> executeInfos;

    private String className;
    private String superClassName;
    private MethodContainer methodContainer;

    public ExecuteClassVisitor(int api, ClassVisitor cv, TotalInfo info) {
        super(api, cv);
        this.executeInfos = info.executeInfos;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        this.className = name;
        this.superClassName = superName;

        String javaName = name.replace('/', '.');
        String javaSuperName = superName.replace('/', '.');
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                interfaces[i] = interfaces[i].replace('/', '.');
            }
        }
        methodContainer = executeInfos.stream()
                .filter(e -> e.match(javaName, javaSuperName, interfaces))
                .reduce(new MethodContainer(), MethodContainer::addExecute, MethodContainer::combine);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        TargetMethodInfo targetMethodInfo = methodContainer.get(name, desc);
        if (targetMethodInfo != null) {
            targetMethodInfo.used = true;
            if ((access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0) {
                ExecuteMethodVisitor m = new ExecuteMethodVisitor(Opcodes.ASM5, access, name, desc, signature, exceptions, mv);
                m.setAopInfo(this.className, targetMethodInfo);
                mv = m;
            }
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        createLostMethod();
        super.visitEnd();
    }

    private void createLostMethod() {
        methodContainer.stream()
                .filter(m -> !m.used && m.createSuper)
                .forEach(this::createMethod);
    }

    private void createMethod(TargetMethodInfo m) {
        MethodNode executeNode = m.executes.get(0).node;
        String[] exceptions = new String[executeNode.exceptions.size()];
        executeNode.exceptions.toArray(exceptions);

        String desc = m.myDescriptor;

        MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC, m.name, desc, null, exceptions);
        GeneratorAdapter mg = new GeneratorAdapter(mv,
                Opcodes.ACC_PUBLIC, m.name, desc);

        // invoke super
        mg.visitCode();
        mg.loadThis();
        mg.loadArgs();
        mg.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassName, m.name, desc, false);
        mg.returnValue();
        int stack = Type.getArgumentsAndReturnSizes(desc) >> 2;
        int local = (stack == 1 && (desc.endsWith("D") || desc.endsWith("J"))) ? 2 : stack;
        mg.visitMaxs(stack, local);
        mg.visitEnd();
    }

    private static class MethodContainer {
        Map<String, TargetMethodInfo> map = new HashMap<>();

        public MethodContainer addExecute(ExecuteInfo executeInfo) {
            String key = executeInfo.targetMethod + " " + executeInfo.targetDesc();
            TargetMethodInfo methodInfo = map.get(key);
            if (methodInfo == null) {
                methodInfo = new TargetMethodInfo();
                methodInfo.myDescriptor = executeInfo.targetDesc();
                methodInfo.name = executeInfo.targetMethod;
                map.put(key, methodInfo);
            }
            methodInfo.addExecute(executeInfo);
            return this;
        }

        public MethodContainer combine(MethodContainer other) {
            if (other != this) {
                map.putAll(other.map);
            }
            return this;
        }

        public Stream<TargetMethodInfo> stream() {
            return map.values().stream();
        }

        public TargetMethodInfo get(String name, String desc) {
            if (map.size() >= 0) {
                return map.get(name + " " + desc);
            }
            return null;
        }
    }
}
