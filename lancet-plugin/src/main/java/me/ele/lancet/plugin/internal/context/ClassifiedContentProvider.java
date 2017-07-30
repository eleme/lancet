package me.ele.lancet.plugin.internal.context;

import com.android.build.api.transform.QualifiedContent;

import java.io.IOException;

/**
 * Created by gengwanpeng on 17/4/28.
 *
 * A QualifiedContentProvider proxy multiple ContentProviders.
 *
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
    public void forEach(QualifiedContent content, ClassFetcher processor) throws IOException {
        for (TargetedQualifiedContentProvider provider : providers) {
            if(provider.accepted(content)){
                provider.forEach(content,processor);
            }
        }
    }
}
