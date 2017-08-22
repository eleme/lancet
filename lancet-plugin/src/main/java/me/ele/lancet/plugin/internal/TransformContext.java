package me.ele.lancet.plugin.internal;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformInvocation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import me.ele.lancet.weaver.internal.graph.Graph;
import me.ele.lancet.weaver.internal.log.Log;

/**
 * Created by gengwanpeng on 17/4/26.
 *
 * A data sets collect all jar info and pre-analysis result.
 *
 */
public class TransformContext {

    private TransformInvocation invocation;

    private Collection<JarInput> allJars;
    private Collection<JarInput> addedJars;
    private Collection<JarInput> removedJars;
    private Collection<JarInput> changedJars;
    private Collection<DirectoryInput> allDirs;

    private GlobalContext global;
    private List<String> hookClasses;
    private Graph graph;

    public TransformContext(TransformInvocation invocation, GlobalContext global) {
        this.global = global;
        this.invocation = invocation;
        init();
    }

    /**
     * start collect.
     */
    private void init() {
        allJars = new ArrayList<>(invocation.getInputs().size());
        addedJars = new ArrayList<>(invocation.getInputs().size());
        changedJars = new ArrayList<>(invocation.getInputs().size());
        removedJars = new ArrayList<>(invocation.getInputs().size());
        allDirs = new ArrayList<>(invocation.getInputs().size());
        invocation.getInputs().forEach(it -> {
            Log.d(it.toString());
            it.getJarInputs().forEach(j -> {
                allJars.add(j);
                if (invocation.isIncremental()) {
                    switch (j.getStatus()) {
                        case ADDED:
                            addedJars.add(j);
                            break;
                        case REMOVED:
                            removedJars.add(j);
                            break;
                        case CHANGED:
                            changedJars.add(j);
                    }
                }
            });
            allDirs.addAll(it.getDirectoryInputs());
        });
    }


    public boolean isIncremental() {
        return invocation.isIncremental();
    }

    public Collection<JarInput> getAllJars() {
        return Collections.unmodifiableCollection(allJars);
    }

    public Collection<DirectoryInput> getAllDirs() {
        return Collections.unmodifiableCollection(allDirs);
    }

    public Collection<JarInput> getAddedJars() {
        return Collections.unmodifiableCollection(addedJars);
    }

    public Collection<JarInput> getChangedJars() {
        return Collections.unmodifiableCollection(changedJars);
    }

    public Collection<JarInput> getRemovedJars() {
        return Collections.unmodifiableCollection(removedJars);
    }

    public File getRelativeFile(QualifiedContent content) {
        return invocation.getOutputProvider().getContentLocation(content.getName(), content.getContentTypes(), content.getScopes(),
                (content instanceof JarInput ? Format.JAR : Format.DIRECTORY));
    }

    public void clear() throws IOException {
        invocation.getOutputProvider().deleteAll();
    }

    public GlobalContext getGlobal() {
        return global;
    }

    public void setHookClasses(List<String> hookClasses) {
        this.hookClasses = hookClasses;
    }

    public List<String> getHookClasses() {
        return hookClasses;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    @Override
    public String toString() {
        return "TransformContext{" +
                "allJars=" + allJars +
                ", addedJars=" + addedJars +
                ", removedJars=" + removedJars +
                ", changedJars=" + changedJars +
                ", allDirs=" + allDirs +
                '}';
    }
}
