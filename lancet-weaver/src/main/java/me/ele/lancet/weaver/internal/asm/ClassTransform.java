package me.ele.lancet.weaver.internal.asm;

import me.ele.lancet.weaver.internal.asm.classvisitor.ContextClassVisitor;
import me.ele.lancet.weaver.internal.graph.Graph;
import org.objectweb.asm.ClassReader;

import me.ele.lancet.weaver.ClassData;
import me.ele.lancet.weaver.internal.asm.classvisitor.CallClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.ExecuteClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.TryCatchInfoClassVisitor;
import me.ele.lancet.weaver.internal.entity.TotalInfo;

/**
 * Created by Jude on 2017/4/25.
 */

public class ClassTransform {

    public static final String AID_INNER_CLASS_NAME = "_lancet";

    public static ClassData[] weave(TotalInfo totalInfo, Graph graph, byte[] classByte, String relativePath) {
        ClassCollector classCollector = new ClassCollector(new ClassReader(classByte), graph);

        String internalName = relativePath.substring(0, relativePath.lastIndexOf('.'));

        classCollector.setOriginClassName(internalName);

        MethodChain chain = new MethodChain(internalName, classCollector.getOriginClassVisitor(), graph);
        ClassContext context = new ClassContext(graph, chain);

        ClassTransform transform = new ClassTransform(classCollector, context);
        transform.connect(new ContextClassVisitor());
        transform.connect(new CallClassVisitor(totalInfo.callInfos));
        transform.connect(new ExecuteClassVisitor(totalInfo.executeInfos));
        transform.connect(new TryCatchInfoClassVisitor(totalInfo.tryCatchInfos));
        transform.startTransform();
        return classCollector.generateClassBytes();
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
