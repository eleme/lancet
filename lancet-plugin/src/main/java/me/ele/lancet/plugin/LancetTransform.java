package me.ele.lancet.plugin;

import com.android.build.api.transform.*;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import me.ele.lancet.plugin.local.*;
import me.ele.lancet.plugin.local.content.ContextThreadPoolProcessor;
import me.ele.lancet.plugin.local.content.QualifiedContentProvider;
import me.ele.lancet.weaver.Weaver;
import me.ele.lancet.weaver.internal.AsmWeaver;
import me.ele.lancet.weaver.internal.log.Impl.FileLoggerImpl;
import me.ele.lancet.weaver.internal.log.Log;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class LancetTransform extends Transform {

    private final LancetExtension lancetExtension;
    //private final AppExtension appExtension;
    private final GlobalContext global;
    private LocalCache cache;


    public LancetTransform(Project project, LancetExtension lancetExtension, AppExtension appExtension) {
        this.lancetExtension = lancetExtension;
        //this.appExtension = appExtension;
        this.global = new GlobalContext(project);

        this.cache = new LocalCache(global.getLancetDir());
    }

    @Override
    public String getName() {
        return "lancet";
    }


    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }


    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }


    @Override
    public Collection<SecondaryFile> getSecondaryFiles() {
        return cache.classesInDirs()
                .stream()
                .map(File::new)
                .map(SecondaryFile::nonIncremental)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<File> getSecondaryDirectoryOutputs() {
        return Collections.singletonList(global.getLancetDir());
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        initLog();
        TransformContext context = new TransformContext(transformInvocation, global);
        PreClassParser preClassParser = new PreClassParser(cache);
        boolean incremental = preClassParser.execute(context);
        Log.i("incremental build: " + incremental);

        new ContextThreadPoolProcessor(context)
                .process(incremental, new TransformProcessor(context, initWeaver(context)));

        Log.i("build successfully done");
    }

    private Weaver initWeaver(TransformContext context) {
        URL[] urls = Stream.concat(context.getAllJars().stream(), context.getAllDirs().stream()).map(QualifiedContent::getFile)
                .map(File::toURI)
                .map(u -> {
                    try {
                        return u.toURL();
                    } catch (MalformedURLException e) {
                        throw new AssertionError(e);
                    }
                })
                .toArray(URL[]::new);
        ClassLoader cl = URLClassLoader.newInstance(urls);
        return AsmWeaver.newInstance(cl, context.getNodesMap(), context.getClasses());
    }

    private void initLog() throws IOException {
        Log.setLevel(lancetExtension.getLogLevel());
        if (!Strings.isNullOrEmpty(lancetExtension.getFileName())) {
            String name = lancetExtension.getFileName();
            if (name.contains(File.separator)) {
                throw new IllegalArgumentException("Log file name can't contains file separator");
            }
            File logFile = new File(global.getLancetDir(), "log_" + lancetExtension.getFileName());
            Files.createParentDirs(logFile);
            Log.setImpl(FileLoggerImpl.of(logFile.getAbsolutePath()));
        }
    }
}

