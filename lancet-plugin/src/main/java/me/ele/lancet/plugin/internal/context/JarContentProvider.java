package me.ele.lancet.plugin.internal.context;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.google.common.io.ByteStreams;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by gengwanpeng on 17/4/28.
 */
public class JarContentProvider extends TargetedQualifiedContentProvider {

    @Override
    public void forEach(QualifiedContent content, ClassFetcher processor) throws IOException {
        forActualInput((JarInput) content, processor);
    }

    private void forActualInput(JarInput jarInput, ClassFetcher processor) throws IOException {
        if (processor.onStart(jarInput)) {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(jarInput.getFile())));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                byte[] data = ByteStreams.toByteArray(zis);
                processor.onClassFetch(jarInput, jarInput.getStatus(), entry.getName(), data);
            }
            IOUtils.closeQuietly(zis);
        }
        processor.onComplete(jarInput);
    }

    @Override
    public boolean accepted(QualifiedContent qualifiedContent) {
        return qualifiedContent instanceof JarInput;
    }
}
