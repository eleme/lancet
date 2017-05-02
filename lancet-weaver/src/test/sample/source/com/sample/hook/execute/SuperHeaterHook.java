package com.sample.hook.execute;

import com.sample.playground.*;
import com.sample.hook.*;

public class SuperHeaterHook{

    @Target(value="com.sample.playground.SuperHeater",createSuper=true)
    public void on() {
        System.out.println("Super~~~~~~~");
        Origin.callVoid();
    }
}