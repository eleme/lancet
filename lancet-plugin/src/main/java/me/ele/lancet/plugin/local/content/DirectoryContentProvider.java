package me.ele.lancet.plugin.local.content;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Created by gengwanpeng on 17/4/28.
 */
public class DirectoryContentProvider extends TargetedQualifiedContentProvider {

    private final boolean incremental;

    public DirectoryContentProvider() {
        this(false);
    }

    public DirectoryContentProvider(boolean incremental) {
        this.incremental = incremental;
    }


    @Override
    public void forEach(QualifiedContent content, SingleClassProcessor processor) throws IOException {
        if (processor.onStart(content)) {
            File root = content.getFile();
            URI base = root.toURI();
            if (!incremental) {
                for (File f : Files.fileTreeTraverser().preOrderTraversal(root)) {
                    if (f.isFile() && f.getName().endsWith(".class")) {
                        byte[] data = Files.toByteArray(f);
                        String relativePath = base.relativize(f.toURI()).toString();
                        processor.onProcess(content, Status.ADDED, relativePath, data);
                    }
                }
            } else {
                for (Map.Entry<File, Status> entry : ((DirectoryInput) content).getChangedFiles().entrySet()) {
                    File f = entry.getKey();
                    if (f.isFile() && f.getName().endsWith(".class")) {
                        byte[] data = Files.toByteArray(f);
                        String relativePath = base.relativize(f.toURI()).toString();
                        processor.onProcess(content, entry.getValue(), relativePath, data);
                    }
                }
            }
        }
        processor.onComplete(content);
    }

    @Override
    public boolean accepted(QualifiedContent qualifiedContent) {
        return qualifiedContent instanceof DirectoryInput;
    }
}
