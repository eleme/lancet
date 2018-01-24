package me.ele.lancet.plugin.internal.preprocess;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.ele.lancet.plugin.Util;
import me.ele.lancet.plugin.internal.LocalCache;
import me.ele.lancet.plugin.internal.TransformContext;
import me.ele.lancet.plugin.internal.context.ClassFetcher;
import me.ele.lancet.plugin.internal.context.ContextReader;
import me.ele.lancet.weaver.internal.log.Log;

/**
 * Created by gengwanpeng on 17/4/26.
 *
 * When you see this class you may be as happy as me like this:
 * <a href="http://wx1.sinaimg.cn/large/415f82b9ly1fe9kqcoe2nj20k00k0dhy.jpg"></a>
 *
 * PreClassAnalysis mainly records the dependency graph of all classes,
 * and record the hook classes to judge if incremental compile available in next time.
 *
 */
public class PreClassAnalysis {

    private LocalCache cache;
    private MetaGraphGeneratorImpl graph;
    private PreClassProcessor classProcessor = new AsmClassProcessorImpl();


    private ContextReader contextReader;


    private volatile boolean partial = true;

    public PreClassAnalysis(LocalCache cache) {
        this.cache = cache;
        this.graph = new MetaGraphGeneratorImpl(cache.hookFlow());
    }

    /**
     * start pre-analysis, the only API for pre-analysis.
     * this method will block until pre-analysis finish.
     *
     * @param incremental
     * @param context
     * @return is incremental compile mode
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean execute(boolean incremental, TransformContext context) throws IOException, InterruptedException {
        Log.d(context.toString());
        long duration = System.currentTimeMillis();

        contextReader = new ContextReader(context);

        if (incremental && context.isIncremental() && !cache.isHookClassModified(context)){
            // can use incremental
            partial = true;

            saveData(partialParse(context), context);
        } else {
            // must full compile
            partial = false;

            // clear LocalCache and TransformContext
            cache.clear();
            context.clear();

            saveData(fullyParse(context), context);

            duration = System.currentTimeMillis() - duration;
            Log.tag("Timer").i("pre parse cost: " + duration);
        }
        return partial;
    }

    private PreAnalysisClassFetcher partialParse(TransformContext context) throws IOException, InterruptedException {
        PreAnalysisClassFetcher preAnalysisClassFetcher = new PreAnalysisClassFetcher();
        // load cached full data into graph.
        cache.accept(graph);
        contextReader.accept(true, preAnalysisClassFetcher);
        return preAnalysisClassFetcher;
    }

    private PreAnalysisClassFetcher fullyParse(TransformContext context) throws IOException, InterruptedException {
        PreAnalysisClassFetcher preAnalysisClassFetcher = new PreAnalysisClassFetcher();
        contextReader.accept(false, preAnalysisClassFetcher);
        return preAnalysisClassFetcher;
    }

    private void saveData(PreAnalysisClassFetcher preAnalysisClassFetcher, TransformContext context) {
        if (partial) {
            cache.savePartially(graph.toLocalNodes());
        } else {
            cache.saveFully(graph.toLocalNodes(), preAnalysisClassFetcher.hookClasses, preAnalysisClassFetcher.hookClassesInDir, new ArrayList<>(preAnalysisClassFetcher.jarPathOfHookClasses));
        }

        context.setGraph(graph.generate());
        context.setHookClasses(cache.hookClasses());
    }


    /**
     * ClassFetcher to fetch all changed classed in this compilation.
     */
    private class PreAnalysisClassFetcher implements ClassFetcher {

        List<String> hookClasses = new ArrayList<>(4);
        List<String> hookClassesInDir = new ArrayList<>(4);
        Set<String> jarPathOfHookClasses = new HashSet<>();

        @Override
        public boolean onStart(QualifiedContent content) {
            return true;
        }

        @Override
        public void onClassFetch(QualifiedContent content, Status status, String relativePath, byte[] bytes) {
            if (relativePath.endsWith(".class")) {
                PreClassProcessor.ProcessResult result = classProcessor.process(bytes);

                // if this time is incremental, the hook class won't be fetched.
                if (partial && result.isHookClass) {
                    partial = false;
                    throw new ParseFailureException(result.toString());
                }

                // store hook classes for next compile
                if (result.isHookClass) {
                    synchronized (this) {
                        hookClasses.add(result.entity.name);
                        if (content instanceof JarInput) {
                            jarPathOfHookClasses.add(content.getFile().getAbsolutePath());
                        } else {
                            hookClassesInDir.add(Util.toSystemDependentFile(content.getFile(), relativePath).getAbsolutePath());
                        }
                    }
                }

                // update the graph
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
