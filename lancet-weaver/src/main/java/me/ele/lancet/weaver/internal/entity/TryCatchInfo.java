package me.ele.lancet.weaver.internal.entity;

import com.google.common.base.Strings;

import java.util.regex.Pattern;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class TryCatchInfo {

    public String regex;
    public String myClass;
    public String myMethod;
    public String methodDescriptor;

    private Pattern pattern;

    public TryCatchInfo(String regex, String myClass, String myMethod, String methodDescriptor) {
        this.regex = regex;
        this.myClass = myClass;
        this.myMethod = myMethod;
        this.methodDescriptor = methodDescriptor;

        if (!Strings.isNullOrEmpty(regex)) {
            this.pattern = Pattern.compile(regex);
        }
    }

    public boolean match(String className) {
        return pattern == null || pattern.matcher(className).matches();
    }

    @Override
    public String toString() {
        return "TryCatchInfo{" +
                "regex='" + regex + '\'' +
                ", myClass='" + myClass + '\'' +
                ", myMethod='" + myMethod + '\'' +
                ", methodDescriptor='" + methodDescriptor + '\'' +
                ", pattern=" + pattern +
                '}';
    }
}
