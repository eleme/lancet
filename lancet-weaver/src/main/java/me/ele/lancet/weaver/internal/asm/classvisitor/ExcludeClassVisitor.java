package me.ele.lancet.weaver.internal.asm.classvisitor;

import me.ele.lancet.base.PlaceHolder;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by gengwanpeng on 17/3/29.
 */
public class ExcludeClassVisitor extends ClassVisitor {

    static private final Set<String> excludePackage;

    static {
        excludePackage = new HashSet<>();
        excludePackage.add("me/ele/lancet/");
        excludePackage.add("android/");
        excludePackage.add("com/android/");
        excludePackage.add("java/");
        excludePackage.add("javax/");
    }


    private final Set<String> excludes;
    private boolean exclude = false;
    private String name;

    public ExcludeClassVisitor(int api, ClassVisitor mv, Set<String> excludes) {
        super(api, mv);
        this.excludes = excludes;
    }

    public boolean isSupplierClass() {
        return PlaceHolder.SUPPLIER_CLASS_NAME.equals(name.replace('/', '.'));
    }

    public boolean isExclude() {
        return exclude;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = name;
        if (excludes.contains(name) || excludePackage.stream().anyMatch(name::startsWith)) {
            exclude = true;
        }
        if (!exclude) {
            super.visit(version, access, name, signature, superName, interfaces);
        }
    }

    @Override
    public void visitSource(String source, String debug) {
        if (!exclude) {
            super.visitSource(source, debug);
        }
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        if (!exclude) {
            super.visitOuterClass(owner, name, desc);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (!exclude) {
            return super.visitAnnotation(desc, visible);
        }
        return null;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        if (!exclude) {
            return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
        }
        return null;
    }

    @Override
    public void visitAttribute(Attribute attr) {
        if (!exclude) {
            super.visitAttribute(attr);
        }
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (!exclude) {
            super.visitInnerClass(name, outerName, innerName, access);
        }
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (!exclude) {
            return super.visitField(access, name, desc, signature, value);
        }
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (!exclude) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            return new JSRInlinerAdapter(mv,
                    access, name, desc, signature, exceptions);
        }
        return null;
    }

    @Override
    public void visitEnd() {
        if (!exclude) {
            super.visitEnd();
        }
    }
}
