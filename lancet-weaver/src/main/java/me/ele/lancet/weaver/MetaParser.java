package me.ele.lancet.weaver;

import java.util.List;

import me.ele.lancet.weaver.internal.entity.TotalInfo;
import me.ele.lancet.weaver.internal.graph.Graph;


/**
 *
 * Created by gengwanpeng on 17/3/21.
 */
public interface MetaParser {

    TotalInfo parse(List<String> classes, Graph graph);
}
