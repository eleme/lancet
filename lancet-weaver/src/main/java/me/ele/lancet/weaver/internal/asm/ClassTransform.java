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
            mHeadVisitor = mTailVisitor = visitor;
        }else {
            mTailVisitor.setNextClassVisitor(visitor);
        }
        visitor.setClassCollector(mClassCollector);
    }

    void startTransform(){
        mTailVisitor.setNextClassVisitor(mClassCollector.newClassWriter(""));
        mClassCollector.mClassReader.accept(mHeadVisitor,0);
    }
}
