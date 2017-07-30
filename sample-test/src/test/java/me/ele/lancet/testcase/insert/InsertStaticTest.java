package me.ele.lancet.testcase.insert;


import com.sample.playground.Cup;

import org.assertj.core.api.Assertions;

import me.ele.lancet.base.Origin;
import me.ele.lancet.base.annotations.Proxy;
import me.ele.lancet.base.annotations.TargetClass;
import me.ele.lancet.plugin.AOPBaseTest;

/**
 * Created by Jude on 2017/4/26.
 */
public class InsertStaticTest extends AOPBaseTest {

    public static class HookClass{
        @TargetClass("com.sample.playground.SugarBox")
        @Proxy("addSugar")
        public static void addSugar(Cup cup, int amount) {
            System.out.println("InsertStaticTest");
            amount = 20;
            Origin.callVoid();
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
                .contains("InsertStaticTest")
                .contains("20");
    }

}