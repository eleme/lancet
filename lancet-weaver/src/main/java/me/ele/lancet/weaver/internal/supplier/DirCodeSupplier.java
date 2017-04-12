package me.ele.lancet.weaver.internal.supplier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.ele.lancet.base.PlaceHolder;
import me.ele.lancet.base.api.ClassSupplier;
import me.ele.lancet.weaver.internal.log.Log;


/**
 * Created by gengwanpeng on 17/3/21.
 */
public class DirCodeSupplier implements ClassSupplier {

    private final ClassLoader cl;

    public DirCodeSupplier(ClassLoader cl) {
        this.cl = cl;
    }

    @Override
    public List<Class<?>> get() {
        Class clazz = null;
        try {
            clazz = cl.loadClass(PlaceHolder.SUPPLIER_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            Log.w("There is no AOP class found in project");
            return new ArrayList<>();
        }
        try {
            Constructor constructor = clazz.getConstructor(ClassLoader.class);
            Object supplier = constructor.newInstance(cl);
            Method method = clazz.getMethod("get");
            List<Class<?>> list = (List<Class<?>>) method.invoke(supplier);
            Log.tag("Process").i("read AOP class: "+list);
            return list;
        } catch (Exception e) {
            Log.w("DirSupplier initialize failed", e);
        }
        return Collections.emptyList();
    }
}
