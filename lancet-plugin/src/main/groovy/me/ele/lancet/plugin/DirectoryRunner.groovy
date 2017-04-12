package me.ele.lancet.plugin

import com.android.build.api.transform.*
import com.android.utils.FileUtils
import com.google.common.base.Preconditions
import com.google.common.io.Files

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


    private static void transformClass(File sourceDir, File targetDir, File sourceFile) throws IOException, TransformException {
        // 断言 源文件一定存在且为.class
        Preconditions.checkArgument sourceFile.exists() && sourceFile.name.endsWith('.class')

        // 取得相对路径
        String path = FileUtils.relativePossiblyNonExistingPath sourceFile, sourceDir
        File targetFile = new File(targetDir, path)
        // 创建父目录
        Files.createParentDirs targetFile

        byte[] bytes = Files.toByteArray sourceFile

        bytes = transform bytes

        Files.write bytes, targetFile
    }


    private static void removeClass(File sourceDir, File targetDir, File sourceFile) throws IOException {
        //取得相对路径
        String path = FileUtils.relativePossiblyNonExistingPath sourceFile, sourceDir
        File targetFile = new File(targetDir, path)

        FileUtils.deleteIfExists targetFile
    }


    private static byte[] transform(byte[] bytes) throws TransformException {
        Transformer.transform bytes
    }
}
