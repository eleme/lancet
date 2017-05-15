package me.ele.lancet.plugin.local.preprocess;


import me.ele.lancet.weaver.internal.graph.ClassEntity;

import java.util.Arrays;
import java.util.List;

/**
 * Created by gengwanpeng on 17/4/27.
 */
public interface PreClassProcessor {

    ProcessResult process(byte[] classBytes);

    class ProcessResult {


        public ProcessResult(boolean isHookClass, ClassEntity entity) {
            this.isHookClass = isHookClass;
            this.entity = entity;
        }

        public boolean isHookClass;
        public ClassEntity entity;

        @Override
        public String toString() {
            return "ProcessResult{" +
                    "isHookClass=" + isHookClass +
                    ", entity=" + entity +
                    '}';
        }
    }
}
