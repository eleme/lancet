package me.ele.lancet.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jude on 4/4/17.
 */

public class ClassFileUtil {

    public static final String MetaDir = (new File("").getAbsolutePath().contains("sample-test")?"":"sample-test/")    +"build/lancet/";
    public static final String ClassDir = (new File("").getAbsolutePath().contains("sample-test")?"":"sample-test/")   +"build/classes/java/main";
    public static final String TestDir = (new File("").getAbsolutePath().contains("sample-test")?"":"sample-test/")   +"build/classes/java/test";
    public static final String ProductDir = (new File("").getAbsolutePath().contains("sample-test")?"":"sample-test/")   +"build/product/java/main";

    public static File getTestFile(String className){
        className = className.replace(".","/");
        return new File(TestDir,className+".class");
    }

    public static File getClassFile(String className){
        className = className.replace(".","/");
        return new File(ClassDir,className+".class");
    }

    public static File getProductFile(String className){
        className = className.replace(".","/");
        return new File(ProductDir,className+".class");
    }

    public static void moveClassToProduce(Class clazz){
        File source = getTestFile(clazz.getName());
        File target = getProductFile(clazz.getName());
        String path = clazz.getPackage().getName().replace(".","/");
        File dir = new File(ProductDir,path);
        if (!dir.exists()){
            dir.mkdirs();
        }
        try {
            com.google.common.io.Files.copy(source,target);
        } catch (IOException e) {
            e.printStackTrace();
        }
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



    public static File clearFile(File file) throws IOException {
        if (!file.exists()){
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }

    public static void resetProductDir(){
        File product = new File(ProductDir);
        travelDeleteFiles(product);
        if (!product.exists()){
            product.mkdir();
        }
        travelDeleteFiles(new File(MetaDir));
    }

    private static void travelDeleteFiles(File file){
        if (file == null || !file.exists()){
            return;
        }
        for (File fileChild : file.listFiles()) {
            if (fileChild.isDirectory()){
                travelDeleteFiles(fileChild);
            }else {
                fileChild.delete();
            }
        }
        file.delete();
    }

}
