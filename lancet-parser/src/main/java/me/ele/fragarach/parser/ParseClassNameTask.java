package me.ele.fragarach.parser;

import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import me.ele.lancet.base.PlaceHolder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * Created by gengwanpeng on 17/4/11.
 */
public class ParseClassNameTask {

    private List<String> classes = new ArrayList<>(4);

    private File in;
    private File buildDir;

    public ParseClassNameTask(File in, File buildDir) {
        this.in = in;
        this.buildDir = buildDir;
    }

    private File createTempFile() {
        return new File(buildDir, "aop-parser/temp." + (in.getName().endsWith("aar") ? "aar" : "jar"));
    }

    public void run() throws IOException {
        File temp = createTempFile();
        Files.createParentDirs(temp);
        ZipInputStream zis = new ZipInputStream(new FileInputStream(in));
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(temp));
        boolean aar = in.getName().endsWith("aar");
        travel(zis, zos, aar);

        zis.close();

        if (!in.delete()) {
            throw new IOException("can't delete origin file " + in);
        }
        if (!temp.renameTo(in)) {
            throw new IOException("can't rename to origin file " + in);
        }
    }

    private void travel(ZipInputStream zis, ZipOutputStream zos, boolean aar) throws IOException {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }
            byte[] bytes = null;

            if (aar) {
                if (entry.getName().equals("classes.jar")) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
                    travel(new ZipInputStream(zis), new ZipOutputStream(bos), false);
                    bytes = bos.toByteArray();
                }
            } else if (entry.getName().endsWith(".class")) {
                bytes = ByteStreams.toByteArray(zis);
                lookup(bytes);
            }

            ZipEntry newEntry;
            if (entry.getMethod() == JarEntry.STORED) {
                newEntry = new JarEntry(entry);
            } else {
                newEntry = new JarEntry(entry.getName());
            }
            zos.putNextEntry(newEntry);
            if (bytes != null) {
                zos.write(bytes);
            } else {
                ByteStreams.copy(zis, zos);
            }
            zos.closeEntry();
            zis.closeEntry();
        }
        if (!aar) {
            entry = new ZipEntry(PlaceHolder.RESOURCE_PATH);
            zos.putNextEntry(entry);
            zos.write(Joiner.on('\n').join(classes).getBytes("utf-8"));
            zos.closeEntry();
        }
        zos.close();
    }

    private void lookup(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        AnnotationClassVisitor cv = new AnnotationClassVisitor(Opcodes.ASM5);
        cr.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        if (cv.hasTargetClass()) {
            classes.add(cv.getName());
        }
    }
}
