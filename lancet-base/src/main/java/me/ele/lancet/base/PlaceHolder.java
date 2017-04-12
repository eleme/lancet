package me.ele.lancet.base;

/**
 * Created by gengwanpeng on 17/3/20.
 */
public final class PlaceHolder {

    public static final String SUPPLIER_CLASS_NAME = "me.ele.lancet.weaver.internal.supplier.DirClassSupplier";
    public static final String CLASS_NAME = PlaceHolder.class.getName().replace('.', '/');
    public static final String RESOURCE_PATH = "META-INF/fragarach/classes.txt";

    private PlaceHolder() {
        throw new AssertionError();
    }

    public static void callVoid() {
    }

    public static <U extends Throwable> void callVoidThrowOne() throws U {
    }

    public static <U extends Throwable, V extends Throwable> void callVoidThrowTwo() throws U, V {
    }

    public static <U extends Throwable, V extends Throwable, W extends Throwable> void callVoidThrowThree() throws U, V, W {
    }

    public static Object call() {
        return new Object();
    }

    public static <V extends Throwable> Object callThrowOne() throws V {
        return new Object();
    }

    public static <V extends Throwable, U extends Throwable> Object callThrowTwo() throws V, U {
        return new Object();
    }

    public static <V extends Throwable, U extends Throwable, W extends Throwable> Object callThrowThree() throws U, V, W {
        return new Object();
    }
}
