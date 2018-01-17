package me.ele.lancet.weaver;

/**
 * Created by Jude on 2017/4/25.
 */

public class ClassData {
    byte[] classBytes;
    String className;

    public ClassData(byte[] classBytes, String className) {
        this.classBytes = classBytes;
        this.className = className;
    }

    public byte[] getClassBytes() {
        return classBytes;
    }

    public void setClassBytes(byte[] classBytes) {
        this.classBytes = classBytes;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
