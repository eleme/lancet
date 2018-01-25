package me.ele.lancet.weaver.internal.entity;

import me.ele.lancet.weaver.internal.util.AsmUtil;
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

    private ThreadLocal<MethodNode> local = new ThreadLocal<MethodNode>(){
        @Override
        synchronized protected MethodNode initialValue() {
            return AsmUtil.clone(sourceMethod);
        }
    };

    public InsertInfo(boolean createSuper, String targetClass, String targetMethod, String targetDesc, String sourceClass, MethodNode sourceMethod) {
        this.createSuper = createSuper;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.targetDesc = targetDesc;
        this.sourceClass = sourceClass;
        this.sourceMethod = sourceMethod;
    }

    public MethodNode threadLocalNode() {
        return local.get();
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
