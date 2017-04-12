package me.ele.lancet.weaver.internal.meta;

import com.google.common.base.Strings;

import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by gengwanpeng on 17/3/21.
 */
public class MethodMetaInfo {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_EXECUTE = 1;
    public static final int TYPE_CALL = 2;
    public static final int TYPE_HANDLER = 4;
    private static final List<Integer> allType;

    static {
        allType = new ArrayList<>(4);
        allType.add(TYPE_NONE);
        allType.add(TYPE_EXECUTE);
        allType.add(TYPE_CALL);
        allType.add(TYPE_HANDLER);
    }

    private int type;

    private String regex;
    private boolean mayCreateSuper;
    private boolean isStatic;

    private String myDescriptor;
    private String myMethod;
    private String myClass;

    private String targetClass;
    private String targetSuperClass;
    private String[] targetInterfaces;

    private String targetMethod;
    private MethodNode node;

    public void addType(int type) {
        if (!allType.contains(type)) {
            throw new IllegalArgumentException("type  is illegal, which is " + type);
        }
        this.type |= type;
    }

    public void addNameRegex(String regex) {
        this.regex = regex;
    }

    public void setMayCreateSuper(boolean mayCreateSuper) {
        this.mayCreateSuper = mayCreateSuper;
    }

    public void checkMethod() {
        // check our method
        int modifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.NATIVE | Modifier.ABSTRACT;
        if ((node.access & modifiers) != (Modifier.PUBLIC | Modifier.STATIC)) {
            throw new IllegalArgumentException(methodDesc() + " with AOP annotation must be public static and must not be abstract or native");
        }

        // check annotation
        if ((Strings.isNullOrEmpty(targetMethod)
                || targetMethod.contains(".")) && type != TYPE_HANDLER) {
            throw new IllegalArgumentException("need @targetMethod with correct method name");
        }

        if (Integer.bitCount(type) > 1) {
            throw new IllegalArgumentException(methodDesc() + " with @Execute @Call @TryCatchHandler can't appear simultaneously.");
        }
        if (type != TYPE_EXECUTE && mayCreateSuper) {
            throw new IllegalArgumentException(methodDesc() + " with @MayCreateSuper only can be used with @Execute.");
        }
        if (type == TYPE_EXECUTE) {
            if (!Strings.isNullOrEmpty(regex)) {
                throw new IllegalArgumentException(methodDesc() + " with @NameRegex must not be used with @Execute.");
            }
        }

        if (type == TYPE_HANDLER) {
            if (!node.desc.equals("(Ljava/lang/Throwable;)V")) {
                throw new IllegalArgumentException(methodDesc() + " with @TryCatchHandler must be declared with void " + node.name + "(Throwable).");
            }
        }
    }

    private String methodDesc() {
        return "Method: " + node.name + node.desc + " ";
    }

    public boolean hasType() {
        return type != TYPE_NONE;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public void setMyDescriptor(String myDescriptor) {
        this.myDescriptor = myDescriptor;
    }

    public void setMyMethod(String myMethod) {
        this.myMethod = myMethod;
    }

    public boolean isMayCreateSuper() {
        return mayCreateSuper;
    }

    public String getMyDescriptor() {
        return myDescriptor;
    }

    public String getMyMethod() {
        return myMethod;
    }

    public String getMyClass() {
        return myClass;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public int getType() {
        return type;
    }

    public String getRegex() {
        return regex;
    }

    public MethodNode getNode() {
        return node;
    }

    public void setNode(MethodNode node) {
        this.node = node;
    }

    public void setMyClass(String myClass) {
        this.myClass = myClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public String getTargetSuperClass() {
        return targetSuperClass;
    }

    public void setTargetSuperClass(String targetSuperClass) {
        this.targetSuperClass = targetSuperClass;
    }

    public String[] getTargetInterfaces() {
        return targetInterfaces;
    }

    public void setTargetInterfaces(String[] targetInterfaces) {
        this.targetInterfaces = targetInterfaces;
    }

    public void setIfEmpty(String targetClassName, String targetSuperClassName, String[] targetInterfaces) {
        if (Strings.isNullOrEmpty(targetClass)
                && Strings.isNullOrEmpty(targetSuperClass)
                && (this.targetInterfaces == null || this.targetInterfaces.length <= 0)) {
            targetClass = targetClassName;
            targetSuperClass = targetSuperClassName;
            this.targetInterfaces = targetInterfaces;
        }
    }

    @Override
    public String toString() {
        return "MethodMetaInfo{" +
                "type=" + type +
                ", regex='" + regex + '\'' +
                ", mayCreateSuper=" + mayCreateSuper +
                ", myDescriptor='" + myDescriptor + '\'' +
                ", myMethod='" + myMethod + '\'' +
                ", myClass='" + myClass + '\'' +
                ", targetClass='" + targetClass + '\'' +
                ", targetSuperClass='" + targetSuperClass + '\'' +
                ", targetInterfaces=" + Arrays.toString(targetInterfaces) +
                ", targetMethod='" + targetMethod + '\'' +
                '}';
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }
}
