package me.ele.lancet.weaver.internal.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by Jude on 2017/4/25.
 */

public class LinkedClassVisitor extends ClassVisitor {
    ClassCollector mClassCollector;

    public LinkedClassVisitor() {
        super(Opcodes.ASM5);
    }

    void setClassCollector(ClassCollector classCollector){
        this.mClassCollector = classCollector;
    }

    void setNextClassVisitor(ClassVisitor classVisitor){
        cv = classVisitor;
    }
}
