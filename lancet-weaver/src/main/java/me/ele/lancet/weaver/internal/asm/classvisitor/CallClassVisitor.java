package me.ele.lancet.weaver.internal.asm.classvisitor;

import me.ele.lancet.weaver.internal.asm.LinkedClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.methodvisitor.CallMethodVisitor;
import me.ele.lancet.weaver.internal.entity.CallInfo;
import me.ele.lancet.weaver.internal.entity.TotalInfo;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.ele.lancet.weaver.internal.asm.classvisitor.methodvisitor.CallMethodVisitor;
import me.ele.lancet.weaver.internal.entity.CallInfo;
import me.ele.lancet.weaver.internal.entity.TotalInfo;
import me.ele.lancet.weaver.internal.log.Log;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class CallClassVisitor extends LinkedClassVisitor {

    public String className;


    private List<CallInfo> infos;
    private Map<String, List<CallInfo>> matches;

    public CallClassVisitor(List<CallInfo> infos) {
        this.infos = infos;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
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
            Log.tag("transform").i("visit Call method: "+className+"."+name+" "+desc);
            mv = new CallMethodVisitor(Opcodes.ASM5, access, name, desc, signature, exceptions, mv, matches,className);
        }
        return mv;
    }
}
