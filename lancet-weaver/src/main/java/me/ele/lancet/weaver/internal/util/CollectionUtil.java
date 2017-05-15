package me.ele.lancet.weaver.internal.util;


import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gengwanpeng on 17/5/8.
 */
public class CollectionUtil {
    public static Collection<String> intersection(Collection<String> l, Collection<String> r) {
        if(l instanceof Set && r instanceof Set){
            return Sets.intersection((Set<String>)l, (Set<String>)r);
        }
        Set<String> s;
        if(l instanceof Set){
            s = (Set<String>) l;
            l = r;
        }else if(r instanceof Set){
            s = (Set<String>) r;
        }else{
            throw new IllegalStateException("must has one set");
        }

        l.removeIf(t -> !s.contains(t));

        return l;
    }
}
