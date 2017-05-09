package me.ele.lancet.plugin.local.preprocess;

import me.ele.lancet.base.annotations.TargetClass;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Created by gengwanpeng on 17/4/27.
 */
public class PreProcessClassVisitor extends ClassVisitor {


    //TODO
    private static String TARGET_CLASS = Type.getDescriptor(TargetClass.class);

    private int access;
    private String name;
    private String superName;
    private String[] interfaces;
    private boolean isHookClass;

    PreProcessClassVisitor(int api) {
        super(api, null);
    }

    public PreClassProcessor.ProcessResult getProcessResult() {
        return new PreClassProcessor.ProcessResult(isHookClass, access, name, superName, interfaces);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.access = access;
        this.name = name;
        this.superName = superName;
        this.interfaces = interfaces;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        judge(desc);
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM5) {
            @Override
            public AnnotationVisitor visitAnnotation(String annoDesc, boolean visible) {
                judge(annoDesc);
                return null;
            }
        };
    }

    private void judge(String desc) {
        if (!isHookClass && TARGET_CLASS.equals(desc)) {
            isHookClass = true;
        }
    }
}
