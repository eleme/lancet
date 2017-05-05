package me.ele.lancet.testsample.hook;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Target {
    String value();
    boolean createSuper() default false;
}