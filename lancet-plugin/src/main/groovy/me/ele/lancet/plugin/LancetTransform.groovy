package me.ele.lancet.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.base.Strings
import me.ele.lancet.weaver.internal.AsmWeaver
import me.ele.lancet.weaver.internal.log.Impl.FileLoggerImpl
import me.ele.lancet.weaver.internal.log.Log

class LancetTransform extends Transform {

    private final LancetExtension lancetExtension
    private final AppExtension appExtension
    private final Configuration configuration

    public LancetTransform(LancetExtension lancetExtension, AppExtension appExtension) {
        this.lancetExtension = lancetExtension
        this.appExtension = appExtension
        this.configuration = new Configuration(appExtension, lancetExtension)
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
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        initLog()
        initProcessor(transformInvocation.inputs)
        configuration.provider = transformInvocation.outputProvider
        if (transformInvocation.incremental) {
            incrementallyTransform transformInvocation
        } else {
            transformInvocation.outputProvider.deleteAll()
            fullyTransform transformInvocation
        }

        new LancetWorker(configuration).execute()
    }

    private void initProcessor(Collection<TransformInput> inputs) {
        List<File> jars = new ArrayList<>()
        List<File> dirs = new ArrayList<>()
        inputs.each {
            jars.addAll it.jarInputs.findAll { it.status != Status.REMOVED }.collect { it.file }
            dirs.addAll it.directoryInputs.collect { it.file }
        }
        jars.addAll appExtension.bootClasspath
        Transformer.asmWeaver = AsmWeaver.newInstance(jars, dirs)
    }

    private void initLog() {
        Log.setLevel lancetExtension.logLevel
        if (!Strings.isNullOrEmpty(lancetExtension.fileName)) {
            Log.setImpl FileLoggerImpl.of(lancetExtension.fileName)
        }
    }


    private void fullyTransform(TransformInvocation transformInvocation) {
        transformInvocation.inputs.each {
            it.jarInputs.each {
                configuration.collectJob it, Status.ADDED, null
            }
            it.directoryInputs.each {
                configuration.collectJob it, Status.ADDED, null
            }
        }
    }

    private void incrementallyTransform(TransformInvocation transformInvocation) {
        transformInvocation.inputs.each {
            it.jarInputs.each {
                if (it.status != Status.NOTCHANGED) {
                    configuration.collectJob it, it.status, null
                }
            }
            it.directoryInputs.each { directoryInput ->
                directoryInput.each {
                    it.changedFiles.each {
                        if (it.key.file && it.value != Status.NOTCHANGED) {
                            configuration.collectJob directoryInput, it.value, it.key
                        }
                    }
                }
            }
        }
    }
}

