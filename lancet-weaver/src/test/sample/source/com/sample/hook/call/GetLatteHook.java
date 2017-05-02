package com.sample.hook.call;

import com.sample.playground.*;
import com.sample.hook.*;

public class GetLatteHook {

    @Target("com.sample.playground.CoffeeBox")
    public String getLatte(CoffeeBox coffeeBox) {
        System.out.println("call GetLatteHook: replace latte with Mocha in coffee box");
        return "Mocha";
    }

}