package me.ele.lancet.weaver.internal.supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import me.ele.lancet.base.PlaceHolder;
import me.ele.lancet.base.api.ClassSupplier;
import me.ele.lancet.weaver.internal.log.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by gengwanpeng on 17/3/21.
 */
public class JarClassSupplier implements ClassSupplier {

    private final Collection<File> jars;
    private final ClassLoader loader;

    public JarClassSupplier(Collection<File> jars, ClassLoader loader) {
        this.jars = jars;
        this.loader = loader;
        Log.i("jars: " + jars);
    }

    @Override
    public List<Class<?>> get() {
        try {
            Enumeration<URL> s = loader.getResources(PlaceHolder.RESOURCE_PATH);
            return ImmutableList.copyOf(Iterators.forEnumeration(s)).stream()
                    .peek(u -> Log.d("url: " + u))
                    .map(this::urlToClassNames)
                    .peek(l -> Log.d("names: " + l))
                    .flatMap(Collection::stream)
                    .map(name -> {
                        try {
                            return loader.loadClass(name);
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException("can't load class: " + name, e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Log.e("load resource failed: ");
        }
        return Collections.emptyList();
    }

    private List<String> urlToClassNames(URL url) {
        InputStream is = null;
        try {
            return CharStreams.readLines(new InputStreamReader(url.openStream()));
        } catch (IOException e) {
            Log.w("read url failed: " + url, e);
        } finally {
            Closeables.closeQuietly(is);
        }
        return Collections.emptyList();
    }
}

