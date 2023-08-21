package me.ele.lancet.weaver.internal.util;

import me.ele.lancet.weaver.internal.log.Log;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by gengwanpeng on 17/4/7.
 */
public class TraceUtil {


    public static ClassVisitor dump(ClassVisitor next) {
        return new ClassNode(Opcodes.ASM6) {
            @Override
            public void visitEnd() {
                super.visitEnd();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ClassVisitor cv = new TraceClassVisitor(null, new ASMifier(), pw);
                accept(cv);
                Log.e(sw.toString());
                if (next != null) {
                    accept(next);
                }
            }
        };
    }

    public static MethodVisitor dump(MethodVisitor next) {
        return new MethodNode(Opcodes.ASM6) {

            @Override
            public void visitEnd() {
                super.visitEnd();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                Printer printer = new ASMifier();
                TraceMethodVisitor traceMv = new TraceMethodVisitor(printer);
                accept(traceMv);
                printer.print(pw);
                System.out.println(sw);
                if (next != null) {
                    accept(next);
                }
            }
        };
    }


    public static MethodVisitor dumpOnly() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Printer printer = new ASMifier();
        TraceMethodVisitor traceMv = new TraceMethodVisitor(printer);
        return new MethodVisitor(Opcodes.ASM6,traceMv) {

            @Override
            public void visitEnd() {
                super.visitEnd();
                printer.print(pw);
                System.out.println(sw);
            }
        };
    }
}
