package me.ele.lancet.testcase.insert;

import com.sample.playground.CoffeeBox;
import com.sample.playground.Cup;

import org.assertj.core.api.Assertions;

import me.ele.lancet.base.Origin;
import me.ele.lancet.base.This;
import me.ele.lancet.base.annotations.Insert;
import me.ele.lancet.base.annotations.TargetClass;
import me.ele.lancet.plugin.AOPBaseTest;

/**
 * Created by Jude on 2017/7/31.
 */

public class GetFieldTest extends AOPBaseTest {

    public static class HookClass{
        @TargetClass("com.sample.playground.CoffeeMaker")
        @Insert("brew")
        public Cup brew(Cup cup) {
            System.out.println("SetFieldTest");
            CoffeeBox coffeeBox = (CoffeeBox) This.getField("coffeeBox");
            System.out.println("get "+coffeeBox.getLatte());
            return (Cup) Origin.call();
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
                .contains("get Latte");
    }
}
