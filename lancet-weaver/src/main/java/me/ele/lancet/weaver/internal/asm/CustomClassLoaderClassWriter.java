package me.ele.lancet.weaver.internal.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Created by gengwanpeng on 17/3/22.
 */
public class CustomClassLoaderClassWriter extends ClassWriter {

    protected ClassLoader loader;

    public CustomClassLoaderClassWriter(int flags) {
        super(flags);
    }

    public CustomClassLoaderClassWriter(ClassReader classReader, ClassLoader loader,int flags) {
        super(classReader, flags);
        if (loader == null) {
            throw new NullPointerException("classloader == null");
        }
        this.loader = loader;
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        Class<?> c, d;
        ClassLoader classLoader = loader;
        if (classLoader == null) {
            throw new IllegalStateException("must call setCustomClassLoader first");
        }
        try {
            c = Class.forName(type1.replace('/', '.'), false, classLoader);
            d = Class.forName(type2.replace('/', '.'), false, classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        if (c.isAssignableFrom(d)) {
            return type1;
        }
        if (d.isAssignableFrom(c)) {
            return type2;
        }
        if (c.isInterface() || d.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                c = c.getSuperclass();
            } while (!c.isAssignableFrom(d));
            return c.getName().replace('.', '/');
        }
    }
}
