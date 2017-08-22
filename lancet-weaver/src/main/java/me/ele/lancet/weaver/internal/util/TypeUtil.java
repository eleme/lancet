package me.ele.lancet.weaver.internal.util;

import org.objectweb.asm.Opcodes;

/**
 * Created by gengwanpeng on 17/3/31.
 */
public class TypeUtil {

    public static String removeFirstParam(String desc) {
        if (desc.startsWith("()")) {
            return desc;
        }
        int index = 1;
        char c = desc.charAt(index);
        while (c == '[') {
            index++;
            c = desc.charAt(index);
        }
        if (c == 'L') {
            while (desc.charAt(index) != ';') {
                index++;
            }
        }
        return "(" + desc.substring(index + 1);
    }

    public static String descToStatic(int access, String desc, String className) {
        if ((access & Opcodes.ACC_STATIC) == 0) {
            desc = "(L" + className.replace('.', '/') + ";" + desc.substring(1);
        }
        return desc;
    }

    public static String descToNonStatic(String desc) {
        return "(" + desc.substring(desc.indexOf(';') + 1);
    }

    public static int parseArray(int index, String desc) {
        while (desc.charAt(index) == '[') index++;
        if (desc.charAt(index) == 'L') {
            while (desc.charAt(index) != ';') index++;
        }
        return index;
    }

    public static int parseObject(int index, String desc) {
        while (desc.charAt(index) != ';') index++;
        return index;
    }

    public static boolean isStatic(int access){
        return (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
    }

    public static boolean isAbstract(int access){
        return (access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT;
    }

    public static boolean isSynthetic(int access){
        return (access & Opcodes.ACC_SYNTHETIC) == Opcodes.ACC_SYNTHETIC;
    }

    public static boolean isPrivate(int access) {
        return (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;
    }

    public static boolean isPublic(int access) {
        return (access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC;
    }

    public static int resetAccessScope(int access,int scope){
        return access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED) | scope;
    }
}
