package me.ele.lancet.plugin.internal;

import com.android.build.api.transform.Status;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import me.ele.lancet.weaver.internal.graph.ClassEntity;
import me.ele.lancet.weaver.internal.graph.CheckFlow;
import org.apache.commons.io.Charsets;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by gengwanpeng on 17/4/26.
 */
public class LocalCache {

    private File localCache;
    private final Metas metas;
    private Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public LocalCache(File dir) {
        localCache = new File(dir, "buildCache.json");
        metas = loadCache();
    }

    private Metas loadCache() {
        if (localCache.exists() && localCache.isFile()) {
            try {
                Reader reader = Files.newReader(localCache, Charsets.UTF_8);
                return gson.fromJson(reader, Metas.class).withoutNull();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JsonParseException e) {
                if (!localCache.delete()) {
                    throw new RuntimeException("cache file has been modified, but can't delete.", e);
                }
            }
        }
        return new Metas();
    }


    public List<String> classes() {
        return metas.classes;
    }

    public List<String> classesInDirs() {
        return metas.classesInDirs;
    }

    public CheckFlow hookFlow() {
        return metas.flow;
    }

    public boolean canBeIncremental(TransformContext context) throws IOException {
        return notModifiedAndRemoved(context); // && notAdded(context);
    }

    private boolean notModifiedAndRemoved(TransformContext context) {
        List<String> targetJars = metas.jarsWithHookClasses;
        return Stream.concat(context.getRemovedJars().stream(), context.getChangedJars().stream())
                .noneMatch(jarInput -> targetJars.contains(jarInput.getFile().getAbsolutePath()));
    }

    public void accept(MetaGraphGeneratorImpl graph) {
        metas.classMetas.forEach(m -> graph.add(m, Status.NOTCHANGED));
    }

    public void saveToLocal() {
        try {
            Files.createParentDirs(localCache);
            Writer writer = Files.newWriter(localCache, Charsets.UTF_8);
            gson.toJson(metas.withoutNull(), Metas.class, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear() throws IOException {
        if (localCache.exists() && localCache.isFile() && !localCache.delete()) {
            throw new IOException("can't delete cache file");
        }
    }

    public void savePartially(List<ClassEntity> classMetas) {
        metas.classMetas = classMetas;
        saveToLocal();
    }

    public void saveFully(List<ClassEntity> classMetas, List<String> classes, List<String> classesInDirs, List<String> jarWithHookClasses) {
        metas.classMetas = classMetas;
        metas.classes = classes;
        metas.classesInDirs = classesInDirs;
        metas.jarsWithHookClasses = jarWithHookClasses;
        saveToLocal();
    }
}
