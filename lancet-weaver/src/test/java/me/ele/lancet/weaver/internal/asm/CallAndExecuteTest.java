package me.ele.lancet.weaver.internal.asm;


import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Jude on 2017/4/26.
 */
public class CallAndExecuteTest extends AOPBaseTest{

    @Override
    public void applyTotalInfo() throws IOException{
        addCallClass("com.sample.hook.call.PutCoffeeHook");
        addCallClass("com.sample.hook.call.GetLatteHook");
        addExecuteClass("com.sample.hook.execute.BrewHook");
        addExecuteClass("com.sample.hook.execute.PutCoffeeHook");
    }

    @Override
    public void checkOutput(String output) {
        assertThat(output)
                .contains("execute BrewHook")
                .contains("call GetLatteHook")
                .contains("call PutCoffeeHook")
                .contains("execute PutCoffeeHook");
    }

}