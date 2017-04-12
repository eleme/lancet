package me.ele.lancet.weaver.internal.meta;


import org.gradle.internal.impldep.com.google.common.io.ByteStreams;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import me.ele.lancet.weaver.internal.util.AopMethodAdjuster;


/**
 * Created by gengwanpeng on 17/3/21.
 */
public class ClassMetaInfo {

    public String myClassName;
    public String targetClassName;
    public String targetSuperClassName;
    public String[] targetInterfaces;

    public List<MethodMetaInfo> infos;
    public ClassNode node;

    private Class<?> clazz;
    private ClassLoader loader;

    public ClassMetaInfo(Class<?> clazz, ClassLoader loader) {
        this.clazz = clazz;
        this.loader = loader;
        infos = new ArrayList<>(4);
        initNode();
    }

    public void addMethod(MethodMetaInfo methodMetaInfo) {
        methodMetaInfo.setMyClass(clazz.getName());
        methodMetaInfo.setIfEmpty(targetClassName, targetSuperClassName, targetInterfaces);
        infos.add(methodMetaInfo);

        if (methodMetaInfo.getType() != MethodMetaInfo.TYPE_HANDLER) {
            adjustMethodCode(methodMetaInfo);
        }
    }

    private void initNode() {
        try {
            InputStream is = loader.getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
            ClassReader classReader = new ClassReader(ByteStreams.toByteArray(is));
            ClassNode cn = new ClassNode();
            classReader.accept(cn, ClassReader.SKIP_DEBUG);
            this.node = cn;
        } catch (IOException e) {
            throw new IllegalStateException("Can't read class file " + clazz.getName(), e);
        }
    }

    private void adjustMethodCode(MethodMetaInfo methodMetaInfo) {
        new AopMethodAdjuster(methodMetaInfo.getNode(), methodMetaInfo).adjust();
    }

    @Override
    public String toString() {
        return "ClassMetaInfo{" +
                "infos=" + infos +
                '}';
    }
}
