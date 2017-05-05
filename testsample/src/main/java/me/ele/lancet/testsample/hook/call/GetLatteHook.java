package me.ele.lancet.testsample.hook.call;


import me.ele.lancet.testsample.hook.Target;
import me.ele.lancet.testsample.playground.CoffeeBox;

public class GetLatteHook {

    @Target("com.sample.playground.CoffeeBox")
    public String getLatte(CoffeeBox coffeeBox) {
        System.out.println("call GetLatteHook: replace latte with Mocha in coffee box");
        return "Mocha";
    }

}