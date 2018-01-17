package me.ele.lancet.weaver.internal.meta;

import me.ele.lancet.weaver.internal.parser.AnnotationMeta;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gengwanpeng on 17/5/3.
 */
public class MethodMetaInfo {

    public MethodNode sourceNode;
    public List<AnnotationMeta> metaList;

    public MethodMetaInfo(MethodNode sourceNode) {
        this.sourceNode = sourceNode;
    }
}
