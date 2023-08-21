package me.ele.lancet.plugin.internal.preprocess;

import me.ele.lancet.base.annotations.*;
import me.ele.lancet.weaver.internal.graph.ClassEntity;
import me.ele.lancet.weaver.internal.graph.FieldEntity;
import me.ele.lancet.weaver.internal.graph.MethodEntity;
import org.objectweb.asm.*;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by gengwanpeng on 17/4/27.
 */
public class PreProcessClassVisitor extends ClassVisitor {

    private static final String PROXY = Type.getDescriptor(Proxy.class);
    private static final String INSERT = Type.getDescriptor(Insert.class);
    private static final String TRY_CATCH = Type.getDescriptor(TryCatchHandler.class);

    private boolean isHookClass;
    private ClassEntity entity;

    PreProcessClassVisitor(int api) {
        super(api, null);
    }

    public PreClassProcessor.ProcessResult getProcessResult() {
        return new PreClassProcessor.ProcessResult(isHookClass, entity);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        entity = new ClassEntity(access, name, superName, interfaces == null ? Collections.emptyList() : Arrays.asList(interfaces));
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        entity.fields.add(new FieldEntity(access, name, desc));
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        entity.methods.add(new MethodEntity(access, name, desc));
        if (!isHookClass) {
            return new MethodVisitor(Opcodes.ASM6) {
                @Override
                public AnnotationVisitor visitAnnotation(String annoDesc, boolean visible) {
                    judge(annoDesc);
                    return null;
                }
            };
        }
        return null;
    }

    private void judge(String desc) {
        if (!isHookClass && (INSERT.equals(desc) || PROXY.equals(desc) || TRY_CATCH.equals(desc))) {
            isHookClass = true;
        }
    }
}
