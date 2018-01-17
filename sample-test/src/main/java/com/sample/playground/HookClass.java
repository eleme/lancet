package com.sample.playground;

import me.ele.lancet.base.Origin;
import me.ele.lancet.base.This;
import me.ele.lancet.base.annotations.Insert;
import me.ele.lancet.base.annotations.TargetClass;

public class HookClass {
    @TargetClass("com.sample.playground.CoffeeMaker")
    @Insert("brew")
    public Cup brew(Cup cup) {
        System.out.println("SetFieldTest");
        CoffeeBox coffeeBox = (CoffeeBox) This.getField("coffeeBox");
        System.out.println("get " + coffeeBox.getLatte());
        return (Cup) Origin.call();
    }

    private static void test(){
        System.out.println("ha");
    }

    static synchronized void test2(){
        return;
    }
}