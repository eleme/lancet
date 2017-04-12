import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by gengwanpeng on 17/4/12.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        File f = new File("classes.jar");
        System.out.println(f.toURL().toString());
        String s = "jar:"+f.toURL().toString()+"!//META-INF/fragarach/classes.txt";
        String s2 = "jar:"+f.toURL().toString()+"!/me/ele/testlib/BuildConfig.class";
        System.out.println(s);
        URL u = new URL(s);
        JarURLConnection jar = (JarURLConnection) u.openConnection();
        InputStream is = u.openStream();
        byte[] b = new byte[8192];
        int len = is.read(b);
        System.out.println(new String(b, 0, len));
    }
}
