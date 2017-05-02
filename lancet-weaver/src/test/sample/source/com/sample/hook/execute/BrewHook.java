package com.sample.hook.execute;
import com.sample.playground.*;
import com.sample.hook.*;

public class BrewHook {

    @Target("com.sample.playground.CoffeeMaker")
    public Cup brew(CoffeeMaker macker,Cup cup) {
        System.out.println("execute BrewHook: Oh,begin to make coffee!");
        return (Cup)Origin.call();
    }

}