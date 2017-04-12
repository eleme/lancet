package me.ele.lancet.weaver.internal.supplier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.ele.lancet.base.api.ClassSupplier;


/**
 * Created by gengwanpeng on 17/3/21.
 */
public class FixedClassSupplier implements ClassSupplier {

    private final ClassLoader loader;

    public FixedClassSupplier(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public List<Class<?>> get() {
        List<Class<?>> ret = Stream.of("ConnectionInterceptorHook", "HttpCodecHook",
                "HttpStreamHook", "OkClientHook", /*"RealConnectionHook", */"StreamAllocationHook")
                .map("me.ele.fragarach.collection.network.okhttp3."::concat)
                .map(s -> {
                    try {
                        return loader.loadClass(s);
                    } catch (ClassNotFoundException ignored) {
                        return null;
                    }
                })
                .filter(c -> c != null)
                .collect(Collectors.toList());
        try {
            ret.add(loader.loadClass("me.ele.demo.HookLifecycle"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
