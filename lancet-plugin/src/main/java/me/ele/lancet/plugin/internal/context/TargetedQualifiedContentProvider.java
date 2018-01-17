package me.ele.lancet.plugin.internal.context;

import com.android.build.api.transform.QualifiedContent;

/**
 * Created by gengwanpeng on 17/4/28.
 * {@inheritDoc}
 */
public abstract class TargetedQualifiedContentProvider implements QualifiedContentProvider {

    /**
     * Judge the QualifiedContent type
     * @param qualifiedContent {@link com.android.build.api.transform.JarInput} or {@link com.android.build.api.transform.DirectoryInput}
     * @return can this provider accept this QualifiedContent.
     */
    public abstract boolean accepted(QualifiedContent qualifiedContent);

}
