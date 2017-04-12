package me.ele.lancet.weaver.internal.asm.classvisitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.ele.lancet.weaver.internal.asm.classvisitor.methodvisitor.CallMethodVisitor;
import me.ele.lancet.weaver.internal.entity.CallInfo;
import me.ele.lancet.weaver.internal.entity.TotalInfo;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class CallClassVisitor extends ClassVisitor {

    public static String debug;


    private List<CallInfo> infos;
    private Map<String, List<CallInfo>> matches;

    public CallClassVisitor(int api, ClassVisitor cv, TotalInfo totalInfo) {
        super(api, cv);
        infos = totalInfo.callInfos;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        matches = infos.stream()
                .filter(t -> t.match(name))
                .collect(Collectors.groupingBy(t -> t.targetClass.replace('.', '/') + " " + t.targetMethod + " " + t.targetDesc()));
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (matches.size() > 0
                && (access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0) {
            mv = new CallMethodVisitor(Opcodes.ASM5, access, name, desc, signature, exceptions, mv, matches);
        }
        return mv;
    }
}
