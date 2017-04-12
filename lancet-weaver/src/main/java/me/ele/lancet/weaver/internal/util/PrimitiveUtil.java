package me.ele.lancet.weaver.internal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gengwanpeng on 17/3/28.
 */
public class PrimitiveUtil {
    private static Map<Character, String> boxMap;
    private static Map<String, String> methodMap;

    static {
        boxMap = new HashMap<>(8);
        boxMap.put('Z', "java/lang/Boolean");
        boxMap.put('C', "java/lang/Character");
        boxMap.put('B', "java/lang/Byte");
        boxMap.put('S', "java/lang/Short");
        boxMap.put('I', "java/lang/Integer");
        boxMap.put('F', "java/lang/Float");
        boxMap.put('J', "java/lang/Long");
        boxMap.put('D', "java/lang/Double");

        methodMap = new HashMap<>(8);
        methodMap.put("java/lang/Boolean", "booleanValue");
        methodMap.put("java/lang/Character", "charValue");
        methodMap.put("java/lang/Byte", "byteValue");
        methodMap.put("java/lang/Short", "shortValue");
        methodMap.put("java/lang/Integer", "intValue");
        methodMap.put("java/lang/Float", "floatValue");
        methodMap.put("java/lang/Long", "longValue");
        methodMap.put("java/lang/Double", "doubleValue");
    }


    public static String box(char primitive) {
        String clazz = boxMap.get(primitive);
        if (clazz == null) {
            throw new IllegalArgumentException("The primitive type '" + primitive + "' is illegal.");
        }
        return clazz;
    }

    public static String unboxMethod(String clazz) {
        String method = methodMap.get(clazz);
        if (method == null) {
            throw new IllegalArgumentException("Box class " + clazz + " is illegal.");
        }
        return method;
    }

    public static Set<Character> primitives() {
        return boxMap.keySet();
    }
}
