package me.ele.lancet.processor;

import com.squareup.javapoet.ClassName;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Jude on 17/4/11.
 */
public class SupplierGeneratorTest {
    @Test
    public void build() throws Exception {
        Set<ClassName> set = new HashSet<>();
        set.add(ClassName.bestGuess("me.ele.lancet.test.ActivityHook"));
        set.add(ClassName.bestGuess("me.ele.lancet.demo.OkHttpHook"));
        SupplierGenerator supplierGenerator = new SupplierGenerator(set);
        StringBuilder stringBuilder = new StringBuilder();
        supplierGenerator.build().writeTo(stringBuilder);
        System.out.println(stringBuilder.toString());
    }

}