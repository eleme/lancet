package me.ele.lancet.weaver.internal.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jude on 4/4/17.
 */

public class ClassFileUtil {
    public static final String ClassDir = "lancet-weaver/src/test/sample/class";
    public static final String ProductDir = "lancet-weaver/src/test/sample/product";


    public static File getClassFile(String className){
        className = className.replace(".","/");
        return new File(ClassDir,className+".class");
    }

    public static List<File> getClassPackageFiles(String packageName){
        packageName = packageName.replace(".","/");
        ArrayList<File> files = new ArrayList<>();
        travelFiles(files,new File(ClassDir,packageName));
        return files;
    }

    private static void travelFiles(ArrayList<File> fileArrayList,File file){
        for (File fileChild : file.listFiles()) {
            if (fileChild.isDirectory()){
                travelFiles(fileArrayList,fileChild);
            }else {
                fileArrayList.add(fileChild);
            }
        }
    }

    public static File getProductFile(String className){
        className = className.replace(".","/");
        return new File(ProductDir,className+".class");
    }

    public static File clearFile(File file) throws IOException {
        if (!file.exists()){
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }

}
