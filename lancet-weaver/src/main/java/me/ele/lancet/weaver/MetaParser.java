package me.ele.lancet.weaver;

import java.util.List;

import me.ele.lancet.weaver.internal.entity.TransformInfo;
import me.ele.lancet.weaver.internal.graph.Graph;


/**
 *
 * Created by gengwanpeng on 17/3/21.
 */
public interface MetaParser {

    TransformInfo parse(List<String> classes, Graph graph);
}
