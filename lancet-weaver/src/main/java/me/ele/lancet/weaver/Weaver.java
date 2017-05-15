package me.ele.lancet.weaver;

/**
 * Created by gengwanpeng on 17/3/21.
 */
public interface Weaver{


    ClassData[] weave(byte[] input, String relativePath);
}
