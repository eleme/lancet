package com.sample.hook.execute;

import com.sample.playground.*;
import com.sample.hook.*;

public class AddSugarHook {

    @Target("com.sample.playground.SugarBox")
    public static void addSugar(Cup cup,int amount) {
        System.out.println("execute AddSugarHook: amount change to "+20);
        amount = 20;
        Origin.callVoid();
    }

}