package me.ele.lancet.weaver.internal.entity;

import org.objectweb.asm.tree.MethodNode;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class ExecuteInfo {

    public boolean createSuper;
    public String targetClass;
    public String targetMethod;
    public String targetDesc;
    public MethodNode node;

    public ExecuteInfo(boolean createSuper, String targetClass, String targetMethod, String targetDesc, MethodNode node) {
        this.createSuper = createSuper;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.targetDesc = targetDesc;
        this.node = node;
    }

    @Override
    public String toString() {
        return "ExecuteInfo{" +
                "  createSuper=" + createSuper +
                ", targetClass='" + targetClass + '\'' +
                ", targetMethod='" + targetMethod + '\'' +
                ", targetDesc='" + targetDesc + '\'' +
                ", node=" + node +
                ", targetDesc='" + targetDesc + '\'' +
                '}';
    }
}
