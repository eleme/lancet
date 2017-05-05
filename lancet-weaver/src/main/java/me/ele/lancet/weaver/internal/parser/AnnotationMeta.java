package me.ele.lancet.weaver.internal.parser;

import me.ele.lancet.weaver.internal.meta.HookInfoLocator;

/**
 * Created by gengwanpeng on 17/5/3.
 */
public abstract class AnnotationMeta {

    public AnnotationMeta(String desc) {
        this.desc = desc;
    }

    public String desc;

    public abstract void accept(HookInfoLocator locator);
}
