package me.ele.lancet.base;

/**
 * Created by gengwanpeng on 17/5/11.
 */
public class This {

    public static final String CLASS_NAME = This.class.getName().replace('.', '/');

    public static Object get() {
        return new Object();
    }

    public static Object getField(String fieldName) {
        return new Object();
    }

    public static void putField(Object field, String fieldName) {
    }
}
