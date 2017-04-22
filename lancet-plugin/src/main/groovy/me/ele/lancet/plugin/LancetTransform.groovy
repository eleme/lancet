package me.ele.lancet.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.google.common.base.Joiner
import com.google.common.base.Strings
import com.google.common.io.Files
import me.ele.lancet.weaver.Weaver
import me.ele.lancet.weaver.internal.AsmWeaver
import me.ele.lancet.weaver.internal.log.Impl.FileLoggerImpl
import me.ele.lancet.weaver.internal.log.Log
import org.gradle.api.Project

class LancetTransform extends Transform {

    private final LancetExtension lancetExtension
    private final AppExtension appExtension
    private final Configuration configuration

    private File outputDir
    private File target;

    public LancetTransform(Project project, LancetExtension lancetExtension, AppExtension appExtension) {
        this.lancetExtension = lancetExtension
        this.appExtension = appExtension
        this.configuration = new Configuration(appExtension, lancetExtension)
        outputDir = new File(project.getBuildDir(), "outputs/lancet")
        target = new File(outputDir, "immutable_classes.txt")
    }

    @Override
    String getName() {
        "lancet"
    }


    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        TransformManager.CONTENT_CLASS
    }


    @Override
    Set<QualifiedContent.Scope> getScopes() {
        TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        true
    }

    @Override
    Collection<SecondaryFile> getSecondaryFiles() {
        if (!target.exists()) {
            return Collections.emptyList()
        }
        target.text.split('\n').collect {
            SecondaryFile.nonIncremental(new File(it))
        }
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        if (!transformInvocation.incremental) {
            transformInvocation.outputProvider.deleteAll()
            target.delete()
        }

        initLog()
        Log.i("incremental build: " + transformInvocation.incremental)
        initWeaver transformInvocation.inputs

        configuration.provider = transformInvocation.outputProvider

        collectTransformJobs transformInvocation

        new LancetWorker(configuration).execute()
    }

    private void initWeaver(Collection<TransformInput> inputs) {
        List<File> jars = new ArrayList<>()
        List<File> dirs = new ArrayList<>()
        inputs.each {
            jars.addAll it.jarInputs.findAll { it.status != Status.REMOVED }.collect { it.file }
            dirs.addAll it.directoryInputs.collect { it.file }
        }
        jars.addAll appExtension.bootClasspath

        Weaver weaver = AsmWeaver.newInstance jars, dirs
        updateUnchangeableClasses weaver.builtInNames, dirs

        Transformer.weaver = weaver
    }

    private void updateUnchangeableClasses(List<String> classes, Collection<File> dirs) {
        Files.createParentDirs target
        Log.d("dirs: " + dirs)
        String text = Joiner.on('\n').join classes.collect {
            String relativePath = FileUtils.toSystemDependentPath(it.replace('.', '/') + ".class")
            File dir = dirs.find { new File(it, relativePath).exists() }
            dir == null ? null : new File(dir, relativePath)
        }.findAll {
            it != null
        }
        target.write text
    }

    private void initLog() {
        Log.setLevel lancetExtension.logLevel
        if (!Strings.isNullOrEmpty(lancetExtension.fileName)) {
            String name = lancetExtension.fileName
            if (name.contains(File.separator)) {
                throw new IllegalArgumentException("Log file name can't contains file separator");
            }
            File logFile = new File(outputDir, "log_${lancetExtension.fileName}")
            Files.createParentDirs(logFile)
            Log.setImpl FileLoggerImpl.of(logFile.absolutePath)
        }
    }

    private void collectTransformJobs(TransformInvocation transformInvocation) {
        transformInvocation.inputs.each {
            Log.d it.toString()
            it.jarInputs.each {
                if (transformInvocation.incremental) {
                    if (it.status != Status.NOTCHANGED) {
                        configuration.collectJob it, it.status, null
                    }
                } else {
                    configuration.collectJob it, Status.ADDED, null
                }
            }
            it.directoryInputs.each { directoryInput ->
                if (transformInvocation.incremental) {
                    directoryInput.each {
                        it.changedFiles.each {
                            if (it.key.file && it.value != Status.NOTCHANGED) {
                                configuration.collectJob directoryInput, it.value, it.key
                            }
                        }
                    }
                } else {
                    configuration.collectJob directoryInput, Status.ADDED, null
                }
            }
        }
    }
}

