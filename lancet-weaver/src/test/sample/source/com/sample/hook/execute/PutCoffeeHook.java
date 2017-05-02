package com.sample.hook.execute;
import com.sample.playground.*;
import com.sample.hook.*;
public class PutCoffeeHook {

    @Target("com.sample.playground.Cup")
    public void putCoffee(Cup cup,String coffee) {
        System.out.println("execute PutCoffeeHook: replace "+coffee+" with "+" nestle when add to cup");
        coffee = "nestle";
        Origin.callVoid();
    }

}