package me.ele.lancet.plugin;

import com.android.build.api.transform.*;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import me.ele.lancet.plugin.internal.*;
import me.ele.lancet.plugin.internal.content.ContextThreadPoolProcessor;
import me.ele.lancet.weaver.MetaParser;
import me.ele.lancet.weaver.Weaver;
import me.ele.lancet.weaver.internal.AsmWeaver;
import me.ele.lancet.weaver.internal.entity.TotalInfo;
import me.ele.lancet.weaver.internal.log.Impl.FileLoggerImpl;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.parser.AsmMetaParser;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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

        Log.i("start time: " + System.currentTimeMillis());

        TransformContext context = new TransformContext(transformInvocation, global);

        Log.i("after android plugin, incremental: " + context.isIncremental());
        Log.i("now: " + System.currentTimeMillis());

        PreClassParser preClassParser = new PreClassParser(cache);
        boolean incremental = preClassParser.execute(context);
        Log.i("after pre parse, incremental: " + incremental);
        Log.i("now: " + System.currentTimeMillis());

        MetaParser parser = createParser(context);
        if (incremental && !context.getGraph().checkFlow()) {
            incremental = false;
            context.clear();
        }
        Log.i("after check flow, incremental: " + incremental);
        Log.i("now: " + System.currentTimeMillis());

        context.getGraph().flow().clear();
        TotalInfo totalInfo = parser.parse(context.getClasses(), context.getGraph());

        Weaver weaver = AsmWeaver.newInstance(totalInfo, context.getGraph());
        new ContextThreadPoolProcessor(context)
                .process(incremental, new TransformProcessor(context, weaver));
        Log.i("build successfully done");
        Log.i("now: " + System.currentTimeMillis());

        cache.saveToLocal();
        Log.i("cache saved");
        Log.i("now: " + System.currentTimeMillis());
    }

    private AsmMetaParser createParser(TransformContext context) {
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
        Log.d("urls:\n" + Joiner.on("\n ").join(urls));
        ClassLoader cl = URLClassLoader.newInstance(urls, null);
        return new AsmMetaParser(cl);
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

