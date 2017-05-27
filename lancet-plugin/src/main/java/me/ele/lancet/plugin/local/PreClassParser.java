package me.ele.lancet.plugin.local;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import me.ele.lancet.plugin.Util;
import me.ele.lancet.plugin.local.content.JarContentProvider;
import me.ele.lancet.plugin.local.content.ContextThreadPoolProcessor;
import me.ele.lancet.plugin.local.content.QualifiedContentProvider;
import me.ele.lancet.plugin.local.extend.BindingJarInput;
import me.ele.lancet.plugin.local.preprocess.AsmClassProcessorImpl;
import me.ele.lancet.plugin.local.preprocess.ParseFailureException;
import me.ele.lancet.plugin.local.preprocess.PreClassProcessor;
import me.ele.lancet.weaver.internal.log.Log;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by gengwanpeng on 17/4/26.
 */
public class PreClassParser {

    private LocalCache cache;
    private MetaGraphGeneratorImpl graph;
    private PreClassProcessor classProcessor = new AsmClassProcessorImpl();


    private ContextThreadPoolProcessor contextProcessor;


    private volatile boolean partial = true;

    public PreClassParser(LocalCache cache) {
        this.cache = cache;
        this.graph = new MetaGraphGeneratorImpl(cache.hookFlow());
    }

    public boolean execute(TransformContext context) throws IOException, InterruptedException {
        Log.d(context.toString());
        long duration = System.currentTimeMillis();

        contextProcessor = new ContextThreadPoolProcessor(context);
        if (context.isIncremental()
                && cache.canBeIncremental(context)
                && tryPartialParse(context)) {
            onComplete(null, context);
            return true;
        }

        clear();
        context.clear();

        onComplete(fullyParse(context), context);

        duration = System.currentTimeMillis() - duration;
        Log.tag("Timer").i("pre parse cost: " + duration);
        return false;
    }

    private void clear() throws IOException {
        partial = false;
        cache.clear();
    }

    private boolean tryPartialParse(TransformContext context) throws IOException, InterruptedException {
        cache.accept(graph);
        contextProcessor.process(true, new SingleProcessor());
        return partial;
    }

    private void onComplete(SingleProcessor singleProcessor, TransformContext context) {
        if (partial) {
            cache.savePartially(graph.toLocalNodes());
        } else {
            cache.saveFully(graph.toLocalNodes(), singleProcessor.classes, singleProcessor.classesInDirs, new ArrayList<>(singleProcessor.jarWithHookClasses));
        }

        context.setGraph(graph.generate());
        context.setClasses(cache.classes());
    }

    private SingleProcessor fullyParse(TransformContext context) throws IOException, InterruptedException {
        SingleProcessor singleProcessor = new SingleProcessor();

        contextProcessor.process(false, singleProcessor);
        return singleProcessor;
    }

    class SingleProcessor implements QualifiedContentProvider.SingleClassProcessor {

        List<String> classes = new ArrayList<>(4);
        List<String> classesInDirs = new ArrayList<>(4);
        Set<String> jarWithHookClasses = new HashSet<>();

        @Override
        public boolean onStart(QualifiedContent content) {
            if (!(content instanceof BindingJarInput)) {
                Log.tag("OnStart").i(content.getName() + " " + content.getFile().getAbsolutePath());
            }
            return true;
        }

        @Override
        public void onProcess(QualifiedContent content, Status status, String relativePath, byte[] bytes) {
            if (relativePath.endsWith(".class")) {
                //Log.tag(content.getName()).i(status + " " + relativePath);
                PreClassProcessor.ProcessResult result = classProcessor.process(bytes);
                if (partial && result.isHookClass) {
                    partial = false;
                    throw new ParseFailureException(result.toString());
                }
                if (result.isHookClass) {
                    synchronized (this) {
                        classes.add(result.entity.name);
                        if (content instanceof JarInput) {
                            jarWithHookClasses.add(content.getFile().getAbsolutePath());
                        } else {
                            classesInDirs.add(Util.toSystemDependentFile(content.getFile(), relativePath).getAbsolutePath());
                        }
                    }
                }
                if (status != Status.REMOVED) {
                    graph.add(result.entity, status);
                } else {
                    graph.remove(result.entity.name);
                }
            }
        }

        @Override
        public void onComplete(QualifiedContent content) {
        }
    }
}
