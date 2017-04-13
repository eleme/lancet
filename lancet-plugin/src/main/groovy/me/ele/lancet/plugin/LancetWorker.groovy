
package me.ele.lancet.plugin

import com.android.build.api.transform.TransformException

import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LancetWorker {

    private final Configuration configuration
    private ExecutorService executor = Executors.newFixedThreadPool Runtime.runtime.availableProcessors()

    public LancetWorker(Configuration configuration) {
        this.configuration = configuration
    }

    public void execute() throws InterruptedException, IOException, TransformException {
        configuration.jobs.collect {
            executor.submit it
        }
        .each {
            try {
                it.get()
            } catch (InterruptedException e) {
                //configuration.provider.deleteAll()
                throw e
            } catch (ExecutionException e) {
                //configuration.provider.deleteAll()
                if (e.getCause() instanceof IOException) {
                    throw e.getCause() as IOException
                } else if (e.getCause() instanceof TransformException) {
                    throw e.getCause() as TransformException
                } else {
                    throw new RuntimeException(e)
                }
            }
        }
        executor.shutdown()
    }
}
