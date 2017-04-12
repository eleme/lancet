package me.ele.fragarach.parser;

import me.ele.lancet.base.annotations.TargetClass;
import org.objectweb.asm.*;

/**
 * Created by gengwanpeng on 17/4/11.
 */
public class AnnotationClassVisitor extends ClassVisitor {

    private static final String TARGET_CLASS = Type.getType(TargetClass.class).getDescriptor();

    private String name;
    private boolean hasTargetClass = false;

    public AnnotationClassVisitor(int api) {
        super(api);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        judge(desc);
        return null;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.name = name.replace('/', '.');
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodVisitor(api) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                judge(desc);
                return null;
            }
        };
    }

    private void judge(String desc) {
        if (TARGET_CLASS.equals(desc)) {
            hasTargetClass = true;
        }
    }

    public boolean hasTargetClass() {
        return hasTargetClass;
    }

    public String getName() {
        return name;
    }
}
