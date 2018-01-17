package me.ele.lancet.plugin;

import com.android.utils.FileUtils;
import me.ele.lancet.weaver.internal.asm.ClassTransform;

import java.io.File;

/**
 * Created by gengwanpeng on 17/5/4.
 */
public class Util {

    public static File toSystemDependentFile(File parent, String relativePath) {
        return new File(parent, relativePath.replace('/', File.separatorChar));
    }

    public static File toSystemDependentHookFile(File relativeRoot, String relativePath) {
        int index = relativePath.lastIndexOf('.');
        return toSystemDependentFile(relativeRoot, relativePath.substring(0, index) + ClassTransform.AID_INNER_CLASS_NAME + relativePath.substring(index));
    }
}
