package me.ele.lancet.plugin.local.content;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.google.common.collect.ImmutableList;
import me.ele.lancet.plugin.local.TransformContext;
import me.ele.lancet.plugin.local.preprocess.ParseFailureException;
import me.ele.lancet.weaver.internal.log.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by gengwanpeng on 17/5/2.
 */
public class ContextThreadPoolProcessor {

    private AtomicBoolean lock = new AtomicBoolean(false);
    private TransformContext context;
    private ClassifiedContentProvider provider = ClassifiedContentProvider.newInstance(new JarContentProvider(), new DirectoryContentProvider());
    private ExecutorService service = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(),
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), (r, executor) -> {
        Log.i("partial parse failed, executor has been shutdown");
    });

    public ContextThreadPoolProcessor(TransformContext context) {
        this.context = context;
    }

    public void process(boolean incremental, QualifiedContentProvider.SingleClassProcessor processor) throws IOException, InterruptedException {
        Collection<JarInput> jars = incremental ? context.getAllJars() :
                ImmutableList.<JarInput>builder().addAll(context.getAddedJars())
                        .addAll(context.getRemovedJars())
                        .addAll(context.getChangedJars())
                        .build();
        List<Future<Void>> tasks = Stream.concat(jars.stream(), context.getAllDirs().stream())
                .map(q -> new QualifiedContentTask(q, processor))
                .map(t -> service.submit(t))
                .collect(Collectors.toList());
        for (Future<Void> future : tasks) {
            try {
                future.get();
            } catch (ExecutionException e) {
                if (incremental && e.getCause() instanceof ParseFailureException) {
                    shutDownAndRestart();
                    continue;
                }
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                } else if (cause instanceof InterruptedException) {
                    throw (InterruptedException) cause;
                }else {
                    throw new RuntimeException(e.getCause());
                }
            }
        }
    }

    private void shutDownAndRestart() {
        if (lock.compareAndSet(false, true)) {
            service.shutdown();
            service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
    }

    private class QualifiedContentTask implements Callable<Void> {

        private QualifiedContent content;
        private QualifiedContentProvider.SingleClassProcessor processor;

        QualifiedContentTask(QualifiedContent content, QualifiedContentProvider.SingleClassProcessor processor) {
            this.content = content;
            this.processor = processor;
        }

        @Override
        public Void call() throws Exception {
            provider.forEach(content, processor);
            return null;
        }
    }
}
