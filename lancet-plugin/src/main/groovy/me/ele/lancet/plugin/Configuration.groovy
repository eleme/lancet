package me.ele.lancet.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.AppExtension
import com.google.common.base.Preconditions

class Configuration {

    private ArrayList<TransformJob> jobs = new ArrayList<>(128)
    private TransformOutputProvider provider
    private AppExtension appExtension
    private LancetExtension ext

    public Configuration(AppExtension appExtension, LancetExtension ext) {
        this.appExtension = appExtension
        this.ext = ext
    }

    public collectJob(QualifiedContent content, Status status, File file) {
        Preconditions.checkNotNull(content)
        Preconditions.checkNotNull(status)
        jobs.add(new TransformJob(provider, content, status, file))
    }

    public setProvider(TransformOutputProvider provider) {
        this.provider = provider
    }

    public List<TransformJob> getJobs() {
        jobs
    }

    public TransformOutputProvider getProvider() {
        provider
    }
}
