package me.ele.lancet.weaver.internal.asm;


import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Jude on 2017/4/26.
 */
public class CallStaticTest extends AOPBaseTest{

    @Override
    public void applyTotalInfo() throws IOException{
        addCallClass("com.sample.hook.call.AddSugarHook");
    }

    @Override
    public void checkOutput(String output) {
        assertThat(output)
                .contains("call AddSugarHook");
    }

}