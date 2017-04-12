package me.ele.lancet.weaver.internal.entity;

import java.util.Map;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class TargetClassInfo {

    public String className;

    // key: name + desc
    public Map<String,TargetMethodInfo> methodMap;

    public TargetClassInfo(String className) {
        this.className = className;
    }
}
