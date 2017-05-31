package me.ele.lancet.plugin.internal.content;

import com.android.build.api.transform.QualifiedContent;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by gengwanpeng on 17/4/28.
 */
public class ClassifiedContentProvider implements QualifiedContentProvider {

    public static ClassifiedContentProvider newInstance(TargetedQualifiedContentProvider... providers) {
        return new ClassifiedContentProvider(providers);
    }

    private TargetedQualifiedContentProvider[] providers;

    private ClassifiedContentProvider(TargetedQualifiedContentProvider... providers) {
        this.providers = providers;
    }

    @Override
    public void forEach(QualifiedContent content, SingleClassProcessor processor) throws IOException {
        Arrays.stream(providers).filter(p -> p.accepted(content)).findFirst()
                .get().forEach(content, processor);
    }
}
