package me.ele.lancet.testcase.insert;

import org.assertj.core.api.Assertions;

import me.ele.lancet.base.Origin;
import me.ele.lancet.base.This;
import me.ele.lancet.base.annotations.Insert;
import me.ele.lancet.base.annotations.TargetClass;
import me.ele.lancet.plugin.AOPBaseTest;

/**
 * Created by Jude on 2017/7/31.
 */

public class SetFieldTest extends AOPBaseTest {

    public static class HookClass{
        @TargetClass("com.sample.playground.Cup")
        @Insert("isEmpty")
        public boolean isEmpty() {
            System.out.println("SetFieldTest");
            This.putField("HookCoffee","coffee");
            return (boolean) Origin.call();
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
                .contains("SetFieldTest")
                .contains("HookCoffee");
    }
}
