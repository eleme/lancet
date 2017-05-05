package me.ele.lancet.weaver.internal.asm;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import java.util.HashMap;
import java.util.Map;

import me.ele.lancet.weaver.ClassData;

/**
 * Created by Jude on 2017/4/25.
 */

public class ClassCollector {

    // canonical name
    String originClassName;
    ClassWriter originClassWriter;

    ClassReader mClassReader;

    // simple name of innerClass
    Map<String, ClassWriter> mClassWriters = new HashMap<>();

    public ClassCollector(ClassReader mClassReader) {
        this.mClassReader = mClassReader;
    }

    void setOriginClassName(String originClassName) {
        this.originClassName = originClassName;
    }

    public ClassVisitor getOriginClassWriter() {
        originClassWriter = new ClassWriter(mClassReader, 0);
        return new CheckClassAdapter(originClassWriter);
//        return originClassWriter;
    }

    public ClassWriter getInnerClassWriter(String classSimpleName) {
        ClassWriter writer = mClassWriters.get(classSimpleName);
        if (writer == null) {
            writer = new ClassWriter(mClassReader, 0);
            writer.visit(Opcodes.V1_7, Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, getCanonicalName(classSimpleName), null, "java/lang/Object", null);
            mClassWriters.put(classSimpleName, writer);
        }
        return writer;
    }

    public ClassData[] generateClassBytes() {
        for (String className : mClassWriters.keySet()) {
            originClassWriter.visitInnerClass(getCanonicalName(className), originClassName, className, Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
        }
        ClassData[] classDataArray = new ClassData[mClassWriters.size() + 1];
        int index = 0;
        for (Map.Entry<String, ClassWriter> entry : mClassWriters.entrySet()) {
            classDataArray[index] = new ClassData();
            classDataArray[index].setClassName(getCanonicalName(entry.getKey()));
            classDataArray[index].setClassBytes(entry.getValue().toByteArray());
            index++;
        }
        classDataArray[index] = new ClassData();
        classDataArray[index].setClassName(originClassName);
        classDataArray[index].setClassBytes(originClassWriter.toByteArray());
        return classDataArray;
    }

    public String getCanonicalName(String simpleName) {
        return originClassName + "$" + simpleName;
    }
}
