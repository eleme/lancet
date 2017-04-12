package me.ele.lancet.weaver;

import java.util.List;

import me.ele.lancet.weaver.internal.meta.ClassMetaInfo;


/**
 *
 * Created by gengwanpeng on 17/3/21.
 */
public interface MetaParser {

    List<ClassMetaInfo> parse(List<Class<?>> classes);
}
