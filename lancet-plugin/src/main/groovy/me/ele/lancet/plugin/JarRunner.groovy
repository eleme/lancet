package me.ele.lancet.plugin

import com.android.build.api.transform.*
import com.android.utils.FileUtils
import com.google.common.base.Preconditions
import com.google.common.io.ByteStreams
import com.google.common.io.Files
import me.ele.lancet.base.PlaceHolder
import me.ele.lancet.weaver.ClassData

import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class JarRunner {

    static void run(TransformOutputProvider provider, JarInput jarInput, Status status) throws IOException, TransformException {
        File targetJar = provider.getContentLocation jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR
        switch (status) {
            case Status.REMOVED:
                remove targetJar
                break
            case Status.ADDED:
            case Status.CHANGED:
                remove targetJar
                transform jarInput.file, targetJar
                break
        }
    }

    /**
     * 移除当前的jar
     */
    private static void remove(File file) throws IOException {
        FileUtils.deleteIfExists file
    }

    /**
     * 增加一个jar
     */
    private static void transform(File sourceJar, File targetJar) throws IOException, TransformException {

        Preconditions.checkArgument sourceJar.exists() && sourceJar.name.endsWith('.jar')

        Files.createParentDirs targetJar

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(targetJar))
        ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceJar))
        ZipEntry entry
        try {
            while ((entry = zis.nextEntry) != null) {
                if (entry.isDirectory() || PlaceHolder.RESOURCE_PATH == entry.name) {
                    continue
                }

                JarEntry newEntry

                if (entry.method == JarEntry.STORED) {
                    newEntry = new JarEntry(entry)
                } else {
                    newEntry = new JarEntry(entry.name)
                }

                if (entry.name.endsWith('.class')) {
                    byte[] bytes = ByteStreams.toByteArray zis
                    // TODO correct write
                    bytes = transform bytes
                    if (bytes != null) {
                        jos.putNextEntry newEntry
                        jos.write bytes
                    }
                } else {
                    jos.putNextEntry newEntry
                    ByteStreams.copy zis, jos
                }

                jos.closeEntry()
                zis.closeEntry()
            }
        } finally {
            zis.close()
            jos.close()
        }
    }

    /**
     * 转换处,只抛转换异常
     */
    private static ClassData[] transform(byte[] bytes) throws TransformException {
        Transformer.transform(bytes)
    }
}
