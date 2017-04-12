package me.ele.lancet.plugin

import com.android.build.api.transform.*
import com.google.common.base.Preconditions
import org.gradle.api.Nullable

class TransformJob implements Runnable {

    private final TransformOutputProvider provider
    private final QualifiedContent content
    private final Status status
    private final File extraFile

    public TransformJob(TransformOutputProvider provider, QualifiedContent content, Status status,
                        @Nullable File extraFile) {
        Preconditions.checkArgument(content instanceof JarInput || content instanceof DirectoryInput)
        this.provider = provider
        this.content = content
        this.status = status
        this.extraFile = extraFile
    }

    @Override
    void run() throws IOException, TransformException {
        if (content instanceof JarInput) {
            JarRunner.run provider, content, status
        } else if (content instanceof DirectoryInput) {
            DirectoryRunner.run provider, content, status, extraFile
        }
    }
}
