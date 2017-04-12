package me.ele.lancet.weaver.internal.supplier;

import java.lang.reflect.Constructor;
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
        try {
            Class clazz = cl.loadClass(PlaceHolder.SUPPLIER_CLASS_NAME);
            Constructor constructor = clazz.getConstructor(ClassLoader.class);
            ClassSupplier supplier = (ClassSupplier) constructor.newInstance(cl);
            return supplier.get();
        } catch (Exception e) {
            Log.w("DirSupplier initialize failed", e);
        }
        return Collections.emptyList();
    }
}
