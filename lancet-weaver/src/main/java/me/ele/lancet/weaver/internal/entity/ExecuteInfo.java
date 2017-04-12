package me.ele.lancet.weaver.internal.entity;

import com.google.common.base.Strings;

import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;

import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.util.TypeUtil;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class ExecuteInfo {


    public boolean isStatic;
    public boolean createSuper;
    public String targetClass;
    public String targetSuperClass;
    public String[] targetInterfaces;
    public String targetMethod;
    public String desc;
    public String myMethod;
    public MethodNode node;

    private String targetDesc;

    public ExecuteInfo(boolean isStatic, boolean createSuper, String targetClass, String targetSuperClass, String[] targetInterfaces, String targetMethod, String desc, String myMethod, MethodNode node) {
        this.isStatic = isStatic;
        this.createSuper = createSuper;
        this.targetClass = targetClass;
        this.targetSuperClass = targetSuperClass;
        this.targetInterfaces = targetInterfaces;
        this.targetMethod = targetMethod;
        this.desc = desc;
        this.myMethod = myMethod;
        this.node = node;
    }

    public String targetDesc() {
        return isStatic ? desc : (targetDesc == null ? targetDesc = TypeUtil.removeFirstParam(desc) : targetDesc);
    }

    public boolean match(String name, String superName, String[] interfaces) {

        try {
            return (Strings.isNullOrEmpty(targetClass) || targetClass.equals(name))
                    && (Strings.isNullOrEmpty(targetSuperClass) || targetSuperClass.equals(superName))
                    && (targetInterfaces == null || interfaces != null && Arrays.asList(interfaces).containsAll(Arrays.asList(targetInterfaces)));
        } catch (RuntimeException r) {
            Log.i(toString());
            throw r;
        }
    }

    @Override
    public String toString() {
        return "ExecuteInfo{" +
                "isStatic=" + isStatic +
                ", createSuper=" + createSuper +
                ", targetClass='" + targetClass + '\'' +
                ", targetSuperClass='" + targetSuperClass + '\'' +
                ", targetInterfaces=" + Arrays.toString(targetInterfaces) +
                ", targetMethod='" + targetMethod + '\'' +
                ", desc='" + desc + '\'' +
                ", myMethod='" + myMethod + '\'' +
                ", node=" + node +
                ", targetDesc='" + targetDesc + '\'' +
                '}';
    }
}
