package me.ele.lancet.plugin.internal.content;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;

import java.io.IOException;

/**
 * Created by gengwanpeng on 17/4/28.
 */
public interface QualifiedContentProvider {

    void forEach(QualifiedContent content, SingleClassProcessor processor) throws IOException;


    interface SingleClassProcessor {

        boolean onStart(QualifiedContent content) throws IOException;

        void onProcess(QualifiedContent content, Status status, String relativePath, byte[] bytes) throws IOException;

        void onComplete(QualifiedContent content) throws IOException;
    }
}
