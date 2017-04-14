import me.ele.lancet.base.PlaceHolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Created by gengwanpeng on 17/4/12.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        File f = new File("/Users/gengwanpeng/.gradle/caches/modules-2/files-2.1/me.ele/lancet-demo/0.0.7/87557ce5de0645065ee6e9a405bf302de2866429/lancet-demo-0.0.7.jar");
        File f2 = new File("/Users/gengwanpeng/.gradle/caches/modules-2/files-2.1/me.ele/fragarach/0.0.7/f186b76fbf774240e2b5c6abfe7d817f5176baa1/fragarach-0.0.7.jar");
        URLClassLoader cl = URLClassLoader.newInstance(new URL[]{f.toURL()});
        JarFile jarFile = new JarFile(f.getAbsolutePath());
        ZipEntry entry = jarFile.getEntry("/" + PlaceHolder.RESOURCE_PATH);
        System.out.println(entry);
        Enumeration<URL> u = cl.findResources(PlaceHolder.RESOURCE_PATH);
        while(u.hasMoreElements()){
            System.out.println(u.nextElement());
        }
    }
}
