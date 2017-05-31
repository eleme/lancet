package me.ele.lancet.plugin.internal.content;

import com.android.build.api.transform.QualifiedContent;

/**
 * Created by gengwanpeng on 17/4/28.
 */
public abstract class TargetedQualifiedContentProvider implements QualifiedContentProvider {


    public abstract boolean accepted(QualifiedContent qualifiedContent);

}
