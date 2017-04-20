package me.ele.lancet.weaver;

import me.ele.lancet.base.api.ClassSupplier;

import java.util.List;

/**
 * Created by gengwanpeng on 17/3/21.
 */
public interface Weaver{

    List<String> getBuiltInNames();

    byte[] weave(byte[] input);
}
