package me.ele.lancet.weaver.internal.parser;

import org.objectweb.asm.tree.AnnotationNode;

/**
 * Created by gengwanpeng on 17/5/3.
 */
public interface AnnoParser {


    AnnotationMeta parseAnno(AnnotationNode annotationNode);
}
