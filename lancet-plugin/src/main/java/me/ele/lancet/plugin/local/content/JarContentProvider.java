package me.ele.lancet.plugin.local.content;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by gengwanpeng on 17/4/28.
 */
public class JarContentProvider extends TargetedQualifiedContentProvider {

    @Override
    public void forEach(QualifiedContent content, SingleClassProcessor processor) throws IOException {
        if (processor.onStart(content)) {
            JarInput jarInput = (JarInput) content;
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(content.getFile())));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()){
                    continue;
                }
                byte[] data = ByteStreams.toByteArray(zis);
                processor.onProcess(content, jarInput.getStatus(), entry.getName(), data);
            }
            IOUtils.closeQuietly(zis);
        }
        processor.onComplete(content);
    }

    @Override
    public boolean accepted(QualifiedContent qualifiedContent) {
        return qualifiedContent instanceof JarInput;
    }
}
