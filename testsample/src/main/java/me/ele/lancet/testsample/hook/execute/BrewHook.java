package me.ele.lancet.testsample.hook.execute;

import me.ele.lancet.testsample.hook.Origin;
import me.ele.lancet.testsample.playground.CoffeeMaker;
import me.ele.lancet.testsample.playground.Cup;
import me.ele.lancet.testsample.hook.Target;

public class BrewHook {

    @Target("com.sample.playground.CoffeeMaker")
    public Cup brew(CoffeeMaker macker, Cup cup) {
        System.out.println("execute BrewHook: Oh,begin to make coffee!");
        return (Cup) Origin.call();
    }

}