package me.ele.lancet.testsample.hook.call;


import me.ele.lancet.testsample.hook.Origin;
import me.ele.lancet.testsample.hook.Target;
import me.ele.lancet.testsample.playground.Cup;

public class PutCoffeeHook {
    @Target("com.sample.playground.Cup")
    public void putCoffee(Cup cup, String coffee) {
        System.out.println("call PutCoffeeHook: replace "+coffee+" with "+"Cappuccino before add to cup");
        coffee = "Cappuccino";
        Origin.callVoid();
    }

}