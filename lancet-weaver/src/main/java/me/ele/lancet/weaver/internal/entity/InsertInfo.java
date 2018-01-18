package me.ele.lancet.weaver.internal.entity;

import org.objectweb.asm.tree.MethodNode;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class InsertInfo {

    public boolean createSuper;
    public String targetClass;
    public String targetMethod;
    public String targetDesc;
    public String sourceClass;
    public MethodNode sourceMethod;
    public boolean isTargetMethodExist;
    public boolean shouldIgoreCheck;

    public InsertInfo(boolean createSuper, String targetClass, String targetMethod, String targetDesc, String sourceClass, MethodNode sourceMethod, boolean shouldIgnoreCheck) {
        this.createSuper = createSuper;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.targetDesc = targetDesc;
        this.sourceClass = sourceClass;
        this.sourceMethod = sourceMethod;
        isTargetMethodExist = false;
        this.shouldIgoreCheck = shouldIgnoreCheck;
    }

    @Override
    public String toString() {
        return "InsertInfo{" +
                "createSuper=" + createSuper +
                ", targetClass='" + targetClass + '\'' +
                ", targetMethod='" + targetMethod + '\'' +
                ", targetDesc='" + targetDesc + '\'' +
                ", sourceClass='" + sourceClass + '\'' +
                ", sourceMethod=" + sourceMethod +
                '}';
    }
}
