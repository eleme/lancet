package me.ele.lancet.testcase.proxy;

import org.assertj.core.api.Assertions;

import me.ele.lancet.base.Origin;
import me.ele.lancet.base.annotations.Proxy;
import me.ele.lancet.base.annotations.TargetClass;
import me.ele.lancet.plugin.AOPBaseTest;

/**
 * Created by Jude on 2017/7/30.
 */

public class ProxyReturnTest extends AOPBaseTest{


    public static class HookClass{
        @TargetClass("com.sample.playground.CoffeeBox")
        @Proxy("getLatte")
        public String getLatte() {
            System.out.println("InsertReturnTest");
            String data = (String)Origin.call();
            return "HookCoffee";
        }
    }

    @Override
    public void setUp() {
        super.setUp();
        addHookClass(HookClass.class);
    }

    @Override
    public void checkOutput(String output) {
        Assertions.assertThat(output)
                .contains("InsertReturnTest")
                .contains("HookCoffee");
    }


}
