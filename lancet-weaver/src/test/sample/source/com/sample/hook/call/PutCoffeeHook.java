package com.sample.hook.call;

import com.sample.playground.*;
import com.sample.hook.*;

public class PutCoffeeHook {
    @Target("com.sample.playground.Cup")
    public void putCoffee(Cup cup,String coffee) {
        System.out.println("call PutCoffeeHook: replace "+coffee+" with "+"Cappuccino before add to cup");
        coffee = "Cappuccino";
        Origin.callVoid();
    }

}