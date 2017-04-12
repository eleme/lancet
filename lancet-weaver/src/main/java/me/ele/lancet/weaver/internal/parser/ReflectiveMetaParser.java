package me.ele.lancet.weaver.internal.parser;

import java.util.List;
import java.util.stream.Collectors;

import me.ele.lancet.weaver.MetaParser;
import me.ele.lancet.weaver.internal.meta.ClassMetaInfo;


/**
 * Created by gengwanpeng on 17/3/21.
 */
public class ReflectiveMetaParser implements MetaParser {


    private ClassLoader loader;

    public ReflectiveMetaParser(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public List<ClassMetaInfo> parse(List<Class<?>> classes) {
        System.out.println(classes);
        return classes.parallelStream()
                .map(clazz -> new AnnotationParser(loader).parse(clazz))
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }
}
