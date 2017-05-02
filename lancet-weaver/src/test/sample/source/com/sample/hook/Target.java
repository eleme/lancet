package com.sample.hook;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Target {
    String value();
    boolean createSuper() default false;
}