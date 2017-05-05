package me.ele.lancet.plugin.local.preprocess;

/**
 * Created by gengwanpeng on 17/4/27.
 */
public interface PreClassProcessor {

    ProcessResult process(byte[] classBytes);

    class ProcessResult {

        public ProcessResult(boolean isHookClass, int access, String className, String superName, String[] interfaces) {
            this.isHookClass = isHookClass;
            this.access = access;
            this.className = className;
            this.superName = superName;
            this.interfaces = interfaces;
        }

        public boolean isHookClass;
        public int access;
        public String className;
        public String superName;
        public String[] interfaces;
    }
}
