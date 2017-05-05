package me.ele.lancet.testsample.hook.execute;

import me.ele.lancet.testsample.hook.Origin;
import me.ele.lancet.testsample.hook.Target;
import me.ele.lancet.testsample.playground.Cup;

public class AddSugarHook {

    @Target("com.sample.playground.SugarBox")
    public static void addSugar(Cup cup, int amount) {
        System.out.println("execute AddSugarHook: amount change to "+20);
        amount = 20;
        Origin.callVoid();
    }

}