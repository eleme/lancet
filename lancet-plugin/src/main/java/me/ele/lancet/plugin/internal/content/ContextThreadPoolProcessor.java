package me.ele.lancet.plugin.internal.content;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import me.ele.lancet.plugin.internal.TransformContext;
import me.ele.lancet.plugin.internal.extend.BindingJarInput;
import me.ele.lancet.plugin.internal.preprocess.ParseFailureException;
import me.ele.lancet.weaver.internal.log.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
    private ClassifiedContentProvider provider;
    private ExecutorService service = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(),
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), (r, executor) -> {
        Log.i("partial parse failed, executor has been shutdown");
    });

    public ContextThreadPoolProcessor(TransformContext context) {
        this.context = context;
    }

    public void process(boolean incremental, QualifiedContentProvider.SingleClassProcessor processor) throws IOException, InterruptedException {

        provider = ClassifiedContentProvider.newInstance(new JarContentProvider(), new DirectoryContentProvider(incremental));
        Collection<JarInput> jars = !incremental ? context.getAllJars() :
                ImmutableList.<JarInput>builder().addAll(context.getAddedJars())
                        .addAll(context.getRemovedJars())
                        .addAll(changedJars())
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
                } else {
                    throw new RuntimeException(e.getCause());
                }
            }
        }

    }

    private Collection<? extends JarInput> changedJars() {
        return context.getChangedJars()
                .stream().map(c -> new BindingJarInput(new JarInput() {

                            private File jar = context.getRelativeFile(c);

                            @Override
                            public Status getStatus() {
                                return Status.REMOVED;
                            }

                            @Override
                            public String getName() {
                                return Hashing.sha1().hashString(jar.getPath(), Charsets.UTF_16LE).toString();
                            }

                            @Override
                            public File getFile() {
                                return jar;
                            }

                            @Override
                            public Set<ContentType> getContentTypes() {
                                return c.getContentTypes();
                            }

                            @Override
                            public Set<? super Scope> getScopes() {
                                return c.getScopes();
                            }
                        }, c)
                ).collect(Collectors.toList());
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
