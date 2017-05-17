package me.ele.lancet.weaver.internal.asm.classvisitor;

import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.ele.lancet.weaver.internal.asm.LinkedClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.methodvisitor.ProxyMethodVisitor;
import me.ele.lancet.weaver.internal.entity.CallInfo;

/**
 * Created by Jude on 17/4/26.
 */
public class ProxyClassVisitor extends LinkedClassVisitor {

    private List<CallInfo> infos;
    private Map<String, List<CallInfo>> matches;

    public ProxyClassVisitor(List<CallInfo> infos) {
        this.infos = infos;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        matches = infos.stream()
                .filter(t -> t.match(name))
                .collect(Collectors.groupingBy(t -> t.targetClass + " " + t.targetMethod + " " + t.targetDesc));

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (matches.size() > 0) {
            mv = new ProxyMethodVisitor(getContext().getChain(), mv, matches, getContext().name, name, getClassCollector());
        }
        return mv;
    }
}
