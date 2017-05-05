package me.ele.lancet.testsample.hook.execute;


import me.ele.lancet.testsample.hook.Origin;
import me.ele.lancet.testsample.hook.Target;

public class SuperHeaterHook{

    @Target(value="com.sample.playground.SuperHeater",createSuper=true)
    public void on() {
        System.out.println("Super~~~~~~~");
        Origin.callVoid();
    }
}