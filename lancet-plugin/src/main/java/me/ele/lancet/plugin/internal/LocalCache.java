package me.ele.lancet.plugin.internal;

import com.android.build.api.transform.Status;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import org.apache.commons.io.Charsets;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.stream.Stream;

import me.ele.lancet.plugin.internal.preprocess.MetaGraphGeneratorImpl;
import me.ele.lancet.weaver.internal.graph.CheckFlow;
import me.ele.lancet.weaver.internal.graph.ClassEntity;

/**
 * Created by gengwanpeng on 17/4/26.
 */
public class LocalCache {

    // Persistent storage for metas
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


    public List<String> hookClasses() {
        return metas.hookClasses;
    }

    public List<String> hookClassesInDir() {
        return metas.hookClassesInDir;
    }

    public CheckFlow hookFlow() {
        return metas.flow;
    }


    /**
     * if hook class has modified.
     * @param context TransformContext for this compile
     * @return true if hook class hasn't modified.
     */
    public boolean isHookClassModified(TransformContext context) {
        List<String> hookClasses = metas.jarsWithHookClasses;
        return Stream.concat(context.getRemovedJars().stream(), context.getChangedJars().stream())
                .anyMatch(jarInput -> hookClasses.contains(jarInput.getFile().getAbsolutePath()));
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

    public void saveFully(List<ClassEntity> classMetas, List<String> hookClasses, List<String> hookClassesInDir, List<String> jarWithHookClasses) {
        metas.classMetas = classMetas;
        metas.hookClasses = hookClasses;
        metas.hookClassesInDir = hookClassesInDir;
        metas.jarsWithHookClasses = jarWithHookClasses;
        saveToLocal();
    }
}
