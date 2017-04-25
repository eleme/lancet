package me.ele.lancet.weaver.internal.asm;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.HashMap;
import java.util.Map;

import me.ele.lancet.weaver.ClassData;

/**
 * Created by Jude on 2017/4/25.
 */

public class ClassCollector {

    ClassReader mClassReader;
    ClassLoader mClassLoader;

    Map<String,ClassWriter> mClassWriters = new HashMap<>();

    public ClassCollector(ClassReader mClassReader,ClassLoader classLoader) {
        this.mClassReader = mClassReader;
        this.mClassLoader = classLoader;
    }

    public ClassWriter newClassWriter(String className){
        CustomClassLoaderClassWriter writer = new CustomClassLoaderClassWriter(mClassReader,0);
        writer.setCustomClassLoader(mClassLoader);
        mClassWriters.put(className,writer);
        return writer;
    }

    public ClassData[] generateClassBytes(){
        ClassData[] classDataArray = new ClassData[mClassWriters.size()];
        int index = 0;
        for (Map.Entry<String, ClassWriter> entry : mClassWriters.entrySet()) {
            classDataArray[index] = new ClassData();
            classDataArray[index].setClassName(entry.getKey());
            classDataArray[index].setClassBytes(entry.getValue().toByteArray());
            index++;
        }
        return classDataArray;
    }
}
