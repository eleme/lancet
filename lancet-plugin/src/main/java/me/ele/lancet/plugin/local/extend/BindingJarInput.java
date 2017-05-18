package me.ele.lancet.plugin.local.extend;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;

import java.io.File;
import java.util.Set;

/**
 * deal with changed jar.
 * Created by gengwanpeng on 17/5/18.
 */
public class BindingJarInput implements JarInput {


    private final JarInput[] inputs;

    public BindingJarInput(JarInput... inputs) {
        this.inputs = inputs;
    }

    public JarInput[] getInputs() {
        return inputs;
    }

    @Override
    public Status getStatus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<ContentType> getContentTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<? super Scope> getScopes() {
        throw new UnsupportedOperationException();
    }
}
