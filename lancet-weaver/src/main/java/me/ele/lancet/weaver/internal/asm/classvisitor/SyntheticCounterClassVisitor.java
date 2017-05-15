package me.ele.lancet.weaver.internal.asm.classvisitor;

import me.ele.lancet.weaver.internal.asm.LinkedClassVisitor;
import me.ele.lancet.weaver.internal.util.Bitset;
import org.objectweb.asm.MethodVisitor;

/**
 * Created by gengwanpeng on 17/5/11.
 */
public class SyntheticCounterClassVisitor extends LinkedClassVisitor {

    private static final String ACCESS = "access$";

    protected Bitset bitset = new Bitset();


    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.startsWith(ACCESS)) {
            bitset.tryAdd(name, ACCESS.length());
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    public MethodVisitor createSyntheticMethod(int access, String name, String desc, String signature, String[] exceptions) {
        String syntheticName = String.format(access + "%03d", bitset.consume());
        return null;
    }
}
