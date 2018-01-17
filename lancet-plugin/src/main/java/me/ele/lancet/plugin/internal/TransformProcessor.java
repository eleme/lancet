package me.ele.lancet.plugin.internal;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.utils.FileUtils;
import com.google.common.io.Files;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import me.ele.lancet.plugin.Util;
import me.ele.lancet.plugin.internal.context.ClassFetcher;
import me.ele.lancet.weaver.ClassData;
import me.ele.lancet.weaver.Weaver;
import me.ele.lancet.weaver.internal.log.Log;

/**
 * Created by gengwanpeng on 17/5/4.
 */
public class TransformProcessor implements ClassFetcher {

    private final TransformContext context;
    private final Weaver weaver;
    private final DirectoryRunner dirRunner = new DirectoryRunner();
    private Map<QualifiedContent, JarRunner> map = new ConcurrentHashMap<>();

    public TransformProcessor(TransformContext context, Weaver weaver) {
        this.context = context;
        this.weaver = weaver;
    }

    @Override
    public boolean onStart(QualifiedContent content) throws IOException {
        if (content instanceof JarInput) {
            JarInput jarInput = (JarInput) content;
            File targetFile = context.getRelativeFile(content);
            switch (jarInput.getStatus()) {
                case REMOVED:
                    FileUtils.deleteIfExists(targetFile);
                    return false;
                case CHANGED:
                    FileUtils.deleteIfExists(targetFile);
                default:
                    Files.createParentDirs(targetFile);
                    map.put(content, new JarRunner(content, targetFile));
            }
        }
        return true;
    }

    @Override
    public void onClassFetch(QualifiedContent content, Status status, String relativePath, byte[] bytes) throws IOException {
        if (content instanceof JarInput) {
            JarRunner jarRunner = map.get(content);
            jarRunner.run(relativePath, bytes);
        } else { // directory, so must be class
            File relativeRoot = context.getRelativeFile(content);
            File target = Util.toSystemDependentFile(relativeRoot, relativePath);
            File hookWithTarget = Util.toSystemDependentHookFile(relativeRoot, relativePath);
            switch (status) {
                case REMOVED:
                    FileUtils.deleteIfExists(target);
                    FileUtils.deleteIfExists(hookWithTarget);
                    break;
                case CHANGED:
                    FileUtils.deleteIfExists(target);
                    FileUtils.deleteIfExists(hookWithTarget);
                default:
                    dirRunner.run(relativeRoot, relativePath, bytes);
            }
        }
    }

    @Override
    public void onComplete(QualifiedContent content) throws IOException {
        if (content instanceof JarInput && ((JarInput) content).getStatus() != Status.REMOVED) {
            map.get(content).close();
        }
    }

    class JarRunner implements Closeable {

        private final JarOutputStream jos;
        private final QualifiedContent content;

        JarRunner(QualifiedContent content, File targetFile) throws IOException {
            this.content = content;
            this.jos = new JarOutputStream(
                    new BufferedOutputStream(new FileOutputStream(targetFile)));
        }

        void run(String relativePath, byte[] bytes) throws IOException {
            if (!relativePath.endsWith(".class")) {
                ZipEntry entry = new ZipEntry(relativePath);
                jos.putNextEntry(entry);
                jos.write(bytes);
            } else {
                for (ClassData classData : weaver.weave(bytes, relativePath)) {
                    ZipEntry entry = new ZipEntry(classData.getClassName() + ".class");
                    jos.putNextEntry(entry);
                    jos.write(classData.getClassBytes());
                }
            }
        }

        public void close() throws IOException {
            jos.close();
        }
    }

    class DirectoryRunner {

        void run(File relativeRoot, String relativePath, byte[] bytes) throws IOException {
            for (ClassData data : weaver.weave(bytes, relativePath)) {
                File target = Util.toSystemDependentFile(relativeRoot, data.getClassName() + ".class");
                Files.createParentDirs(target);
                Files.write(data.getClassBytes(), target);
            }
        }
    }
}
