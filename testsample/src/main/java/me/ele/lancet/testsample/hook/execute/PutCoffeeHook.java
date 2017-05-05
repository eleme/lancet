package me.ele.lancet.testsample.hook.execute;

import me.ele.lancet.testsample.hook.Origin;
import me.ele.lancet.testsample.playground.Cup;
import me.ele.lancet.testsample.hook.Target;

public class PutCoffeeHook {

    @Target("com.sample.playground.Cup")
    public void putCoffee(Cup cup, String coffee) {
        System.out.println("execute PutCoffeeHook: replace "+coffee+" with "+" nestle when add to cup");
        coffee = "nestle";
        Origin.callVoid();
    }

}