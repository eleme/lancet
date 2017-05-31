package me.ele.lancet.weaver.internal.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by gengwanpeng on 17/3/28.
 */
public class PrimitiveUtil {
    private static Map<String, String> boxMap;
    private static Map<String, String> unboxMap;
    private static Map<String, String> methodMap;
    private static Set<String> numberTypes;

    static {
        boxMap = new HashMap<>(8);
        boxMap.put("Z", "java/lang/Boolean");
        boxMap.put("C", "java/lang/Character");
        boxMap.put("B", "java/lang/Byte");
        boxMap.put("S", "java/lang/Short");
        boxMap.put("I", "java/lang/Integer");
        boxMap.put("F", "java/lang/Float");
        boxMap.put("J", "java/lang/Long");
        boxMap.put("D", "java/lang/Double");

        unboxMap = new HashMap<>(8);
        unboxMap.put("java/lang/Boolean", "Z");
        unboxMap.put("java/lang/Character", "C");
        unboxMap.put("java/lang/Byte", "B");
        unboxMap.put("java/lang/Short", "S");
        unboxMap.put("java/lang/Integer", "I");
        unboxMap.put("java/lang/Float", "F");
        unboxMap.put("java/lang/Long", "J");
        unboxMap.put("java/lang/Double", "D");

        methodMap = new HashMap<>(8);
        methodMap.put("java/lang/Boolean", "booleanValue");
        methodMap.put("java/lang/Character", "charValue");
        methodMap.put("java/lang/Byte", "byteValue");
        methodMap.put("java/lang/Short", "shortValue");
        methodMap.put("java/lang/Integer", "intValue");
        methodMap.put("java/lang/Float", "floatValue");
        methodMap.put("java/lang/Long", "longValue");
        methodMap.put("java/lang/Double", "doubleValue");

        numberTypes = new HashSet<>();
        numberTypes.add("java/lang/Byte");
        numberTypes.add("java/lang/Short");
        numberTypes.add("java/lang/Integer");
        numberTypes.add("java/lang/Float");
        numberTypes.add("java/lang/Long");
        numberTypes.add("java/lang/Double");
    }


    public static String box(String primitive) {
        String clazz = boxMap.get(primitive);
        if (clazz == null) {
            throw new IllegalArgumentException("The primitive type '" + primitive + "' is illegal.");
        }
        return clazz;
    }

    public static String unbox(String type) {
        String ret = unboxMap.get(type);
        if (ret == null) {
            throw new IllegalArgumentException("The unbox type '" + type + "' is illegal.");
        }
        return ret;
    }

    public static String unboxMethod(String clazz) {
        String method = methodMap.get(clazz);
        if (method == null) {
            throw new IllegalArgumentException("Box class " + clazz + " is illegal.");
        }
        return method;
    }

    public static boolean isPrimitive(String s) {
        return boxMap.containsKey(s);
    }


    public static Set<String> boxedTypes() {
        return methodMap.keySet();
    }

    public static String virtualType(String owner) {
        if (numberTypes.contains(owner)) {
            return "java/lang/Number";
        }
        return owner;
    }

    public static Set<String> boxedNumberTypes() {
        return numberTypes;
    }


}
