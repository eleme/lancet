package me.ele.lancet.weaver.internal.supplier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.ele.lancet.base.api.ClassSupplier;


/**
 * Created by gengwanpeng on 17/3/21.
 */
public class ComponentSupplier implements ClassSupplier {

    public static ComponentSupplier newInstance(ClassSupplier... suppliers) {
        return new ComponentSupplier(suppliers);
    }

    private final ClassSupplier[] suppliers;

    private ComponentSupplier(ClassSupplier[] suppliers) {
        this.suppliers = suppliers;
    }

    @Override
    public List<Class<?>> get() {
        return Arrays.stream(suppliers)
                .parallel()
                .flatMap(s -> s.get().stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
