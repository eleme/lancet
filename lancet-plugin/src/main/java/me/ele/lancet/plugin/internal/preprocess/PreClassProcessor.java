package me.ele.lancet.plugin.internal.preprocess;


import me.ele.lancet.weaver.internal.graph.ClassEntity;

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
