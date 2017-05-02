package me.ele.lancet.weaver.internal.asm;

import org.objectweb.asm.ClassReader;

import me.ele.lancet.weaver.ClassData;
import me.ele.lancet.weaver.internal.asm.classvisitor.CallClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.ExcludeClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.ExecuteClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.TryCatchInfoClassVisitor;
import me.ele.lancet.weaver.internal.entity.TotalInfo;

/**
 * Created by Jude on 2017/4/25.
 */

public class ClassTransform {

    public static final String AID_INNER_CLASS_NAME = "_lancet";

    public static ClassData[] weave(ClassLoader classLoader, TotalInfo totalInfo, byte[] classByte) {
        ClassCollector classCollector = new ClassCollector(new ClassReader(classByte),classLoader);

        ClassTransform transform = new ClassTransform(classCollector);
        transform.connect(new ExcludeClassVisitor(totalInfo.excludes));
        transform.connect(new CallClassVisitor(totalInfo.callInfos));
        transform.connect(new ExecuteClassVisitor(totalInfo.executeInfos));
        transform.connect(new TryCatchInfoClassVisitor(totalInfo.tryCatchInfos));
        transform.startTransform();
        return classCollector.generateClassBytes();
    }

    private LinkedClassVisitor mHeadVisitor;
    private LinkedClassVisitor mTailVisitor;
    private ClassCollector mClassCollector;

    public ClassTransform(ClassCollector mClassCollector) {
        this.mClassCollector = mClassCollector;
    }

    void connect(LinkedClassVisitor visitor){
        if (mHeadVisitor == null){
            mHeadVisitor = visitor;
        }else {
            mTailVisitor.setNextClassVisitor(visitor);
        }
        mTailVisitor = visitor;
        visitor.setClassCollector(mClassCollector);
    }

    void startTransform(){
        mTailVisitor.setNextClassVisitor(mClassCollector.getOriginClassWriter());
        mClassCollector.mClassReader.accept(mHeadVisitor,0);
    }
}
