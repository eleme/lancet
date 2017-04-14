package me.ele.test;

import me.ele.lancet.base.PlaceHolder;
import me.ele.lancet.base.annotations.Execute;
import me.ele.lancet.base.annotations.TargetClass;
import me.ele.lancet.base.annotations.TargetMethod;

import java.util.Arrays;

/**
 * Created by gengwanpeng on 17/4/14.
 */
@TargetClass("Main")
public class HookMain {


    @Execute
    @TargetMethod(value = "main", isStatic = true)
    public static void main(String[] args){
        args = new String[]{"ok"};
        PlaceHolder.callVoid();
    }
}
