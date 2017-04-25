package me.ele.lancet.plugin

import com.android.build.api.transform.*
import com.android.utils.FileUtils
import com.google.common.base.Preconditions
import com.google.common.io.Files
import me.ele.lancet.weaver.ClassData

class DirectoryRunner {

    static void run(TransformOutputProvider provider,
                    DirectoryInput input,
                    Status status,
                    File sourceFile) throws IOException, TransformException {
        File targetDir = provider.getContentLocation input.name, input.contentTypes, input.scopes, Format.DIRECTORY
        switch (status) {
            case Status.ADDED:
                if (sourceFile == null) {
                    transformDir input.file, targetDir
                } else {
                    transformClass input.file, targetDir, sourceFile
                }
                break
            case Status.REMOVED:
                removeClass input.file, targetDir, sourceFile
                break
            case Status.CHANGED:
                removeClass input.file, targetDir, sourceFile
                transformClass input.file, targetDir, sourceFile
                break
        }
    }


    private static void transformDir(File sourceDir, File targetDir) throws IOException, TransformException {
        Files.fileTreeTraverser().breadthFirstTraversal(sourceDir).each {
            if (it.name.endsWith('.class')) {
                transformClass sourceDir, targetDir, it
            }
        }
    }


    private
    static void transformClass(File sourceDir, File targetDir, File sourceFile) throws IOException, TransformException {
        // assert source file exists and must end with .class
        Preconditions.checkArgument sourceFile.exists() && sourceFile.name.endsWith('.class')

        // get relative path
        String path = FileUtils.relativePossiblyNonExistingPath sourceFile, sourceDir
        File targetFile = new File(targetDir, path)
        // create parent dir
        Files.createParentDirs targetFile

        byte[] bytes = Files.toByteArray sourceFile
        ClassData[] result = transform bytes

        if (result != null) {
            result.each {
                //TODO write correct file
//                Files.write it, targetFile
            }
        }
    }


    private static void removeClass(File sourceDir, File targetDir, File sourceFile) throws IOException {
        String path = FileUtils.relativePossiblyNonExistingPath sourceFile, sourceDir
        File targetFile = new File(targetDir, path)

        FileUtils.deleteIfExists targetFile
    }


    private static byte[][] transform(byte[] bytes) throws TransformException {
        Transformer.transform bytes
    }
}
