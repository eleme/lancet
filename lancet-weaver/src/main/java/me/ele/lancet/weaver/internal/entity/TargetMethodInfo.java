package me.ele.lancet.weaver.internal.entity;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class TargetMethodInfo {

    public boolean used = false;
    public boolean createSuper = false;

    public String myDescriptor;
    public String name;

    public List<ExecuteInfo> executes = new ArrayList<>();

    public TargetMethodInfo() {
    }


    public void addExecute(ExecuteInfo info) {
        executes.add(info);
        createSuper |= info.createSuper;
    }
}
