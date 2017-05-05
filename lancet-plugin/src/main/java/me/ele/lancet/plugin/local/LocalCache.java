package me.ele.lancet.plugin.local;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.commons.io.Charsets;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by gengwanpeng on 17/4/26.
 */
public class LocalCache {

    private File localCache;
    private Metas metas;
    private Gson gson = new Gson();

    public LocalCache(File dir) {
        localCache = new File(dir, "buildCache.json");
        loadCache();
    }

    private void loadCache() {
        if (localCache.exists() && localCache.isFile()) {
            try {
                Reader reader = Files.newReader(localCache, Charsets.UTF_8);
                metas = gson.fromJson(reader, Metas.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JsonParseException e) {
                if (!localCache.delete()) {
                    throw new RuntimeException("cache file has been modified, but can't delete.", e);
                }
            }
        }else{
            metas = new Metas();
        }
    }


    public List<String> classes() {
        return metas.classes;
    }

    public List<String> classesInDirs() {
        return metas.classesInDirs;
    }

    public boolean canBeIncremental(TransformContext context) throws IOException {
        return notModifiedAndRemoved(context); // && notAdded(context);
    }

   /* private static boolean notAdded(TransformContext transformContext) throws IOException {
        QualifiedContentProvider provider = ClassifiedContentProvider.newInstance(new JarContentProvider(), new DirectoryContentProvider());
        PreClassProcessor processor = new AsmClassProcessorImpl();
        boolean[] added = {false};
        for (JarInput jarInput : transformContext.getAddedJars()) {
            provider.forEach(jarInput, (content, status, relativePath, classBytes) -> {
                added[0] |= processor.process(classBytes).isHookClass;
            });
        }
        return !added[0];
    }*/

    private boolean notModifiedAndRemoved(TransformContext context) {
        List<String> targetJars = metas.jarsWithHookClasses;
        return Stream.concat(context.getRemovedJars().stream(), context.getChangedJars().stream())
                .noneMatch(jarInput -> targetJars.contains(jarInput.getFile().getAbsolutePath()));
    }

    public void accept(MetaGraphGeneratorImpl graph) {
        metas.nodeLikes.forEach(n -> {
            graph.add(n.access, n.name, n.superName, n.interfaces);
        });
    }

    public void save() {
        try {
            Writer writer = Files.newWriter(localCache, Charsets.UTF_8);
            gson.toJson(metas, Metas.class, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear() throws IOException {
        if (localCache.exists() && localCache.isFile() && !localCache.delete()) {
            throw new IOException("can't delete cache file");
        }
        metas = new Metas();
    }

    public void savePartially(List<Metas.NodeLike> nodeLikes) {
        metas.nodeLikes = nodeLikes;
        if (metas.classes == null) {
            metas.classes = Collections.emptyList();
        }
        if (metas.classesInDirs == null) {
            metas.classesInDirs = Collections.emptyList();
        }
        if (metas.jarsWithHookClasses == null) {
            metas.jarsWithHookClasses = Collections.emptyList();
        }
        save();
    }

    public void saveFully(List<Metas.NodeLike> nodeLikes, List<String> classes, List<String> classesInDirs, List<String> jarWithHookClasses) {
        metas.nodeLikes = nodeLikes;
        metas.classes = classes;
        metas.classesInDirs = classesInDirs;
        metas.jarsWithHookClasses = jarWithHookClasses;
        save();
    }
}
