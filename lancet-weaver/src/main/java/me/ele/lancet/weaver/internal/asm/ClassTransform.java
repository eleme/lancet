package me.ele.lancet.weaver.internal.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import me.ele.lancet.weaver.ClassData;
import me.ele.lancet.weaver.internal.asm.classvisitor.HookClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.InsertClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.ProxyClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.TryCatchInfoClassVisitor;
import me.ele.lancet.weaver.internal.entity.InsertInfo;
import me.ele.lancet.weaver.internal.entity.TransformInfo;
import me.ele.lancet.weaver.internal.graph.Graph;

/**
 * Created by Jude on 2017/4/25.
 */

public class ClassTransform {

    public static final String AID_INNER_CLASS_NAME = "_lancet";

    public static ClassData[] weave(TransformInfo transformInfo, Graph graph, byte[] classByte, String internalName) {
        ClassCollector classCollector = new ClassCollector(new ClassReader(classByte), graph);

        classCollector.setOriginClassName(internalName);

        checkIfInsertMethodExist(transformInfo, classByte, internalName);
        MethodChain chain = new MethodChain(internalName, classCollector.getOriginClassVisitor(), graph);
        ClassContext context = new ClassContext(graph, chain, classCollector.getOriginClassVisitor());

        ClassTransform transform = new ClassTransform(classCollector, context);
        transform.connect(new HookClassVisitor(transformInfo.hookClasses));
        transform.connect(new ProxyClassVisitor(transformInfo.proxyInfo));
        transform.connect(new InsertClassVisitor(transformInfo.executeInfo));
        transform.connect(new TryCatchInfoClassVisitor(transformInfo.tryCatchInfo));
        transform.startTransform();
        return classCollector.generateClassBytes();
    }

    private static void checkIfInsertMethodExist(TransformInfo transformInfo, byte[] classByte, String internalName) {
        List<InsertInfo> matchInsertMethods = transformInfo.executeInfo.get(internalName);
        if (matchInsertMethods == null || matchInsertMethods.isEmpty())
            return;
        ClassReader cr = new ClassReader(classByte);
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_CODE);
        List<MethodNode> methods = cn.methods;
        methods.forEach(m -> matchInsertMethods.forEach(e -> {
            if (e.targetMethod.equals(m.name) && e.targetDesc.equals(m.desc)) {
                if (((e.sourceMethod.access ^ m.access) & Opcodes.ACC_STATIC) != 0) {
                    throw new IllegalStateException(e.sourceClass + "." + e.sourceMethod.name + " should have the same static flag with "
                            + internalName + "." + m.name);
                }
                if ((m.access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) == 0) {
                    e.isTargetMethodExist = true;
                }
            }
        }));
    }

    private LinkedClassVisitor mHeadVisitor;
    private LinkedClassVisitor mTailVisitor;
    private ClassCollector mClassCollector;
    private final ClassContext context;

    public ClassTransform(ClassCollector mClassCollector, ClassContext context) {
        this.mClassCollector = mClassCollector;
        this.context = context;
    }

    void connect(LinkedClassVisitor visitor) {
        if (mHeadVisitor == null) {
            mHeadVisitor = visitor;
        } else {
            mTailVisitor.setNextClassVisitor(visitor);
        }
        mTailVisitor = visitor;
        visitor.setClassCollector(mClassCollector);
        visitor.setContext(context);
    }

    void startTransform() {
        mTailVisitor.setNextClassVisitor(mClassCollector.getOriginClassVisitor());
        mClassCollector.mClassReader.accept(mHeadVisitor, 0);
    }
}
