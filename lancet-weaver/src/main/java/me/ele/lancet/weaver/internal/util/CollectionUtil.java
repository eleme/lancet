package me.ele.lancet.weaver.internal.util;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gengwanpeng on 17/5/8.
 */
public class CollectionUtil {
    public static List<String> intersection(List<String> l, List<String> r) {
        if (r.size() < l.size()) {
            List<String> t = r;
            r = l;
            l = t;
        }
        if (l.size() < 4) {
            List<String> finalL = l;
            r.removeIf(n -> !finalL.contains(n));
        } else {
            Set<String> set = new HashSet<>(l);
            r.removeIf(n -> !set.contains(n));
        }
        return r;
    }
}
