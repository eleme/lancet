package me.ele.lancet.plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;

import org.gradle.api.Project;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.ele.lancet.weaver.internal.log.Impl.SystemOutputImpl;
import me.ele.lancet.weaver.internal.log.Log;
import okio.BufferedSource;
import okio.Okio;

import static org.mockito.Mockito.when;


/**
 * Created by Jude on 2017/5/2.
 */

public abstract class AOPBaseTest {

    private List<Class> mHookClass = new ArrayList<>();

    @Before
    public void setUp() {
        ClassFileUtil.resetProductDir();
        Log.setImpl(new SystemOutputImpl());
    }

    private TransformInvocation mockTransformInvocation(){
        TransformInvocation transformInvocation = Mockito.mock(TransformInvocation.class);

        TransformInput transformInput = Mockito.mock(TransformInput.class);
        // jar input
        when(transformInput.getJarInputs()).thenReturn(Collections.emptyList());
        // dir input
        DirectoryInput sampleDirectoryInput = Mockito.mock(DirectoryInput.class);
        System.out.println(new File("").getAbsoluteFile());
        when(sampleDirectoryInput.getChangedFiles()).thenReturn(generateClassMap(new File(ClassFileUtil.ClassDir)));
        when(sampleDirectoryInput.getFile()).thenReturn(new File(ClassFileUtil.ClassDir));

        DirectoryInput hookDirectoryInput = Mockito.mock(DirectoryInput.class);
        when(hookDirectoryInput.getChangedFiles()).thenReturn(generateHookClassMap());
        when(hookDirectoryInput.getFile()).thenReturn(new File(ClassFileUtil.ProductDir));

        ArrayList<DirectoryInput> inputs = new ArrayList<>();
        inputs.add(sampleDirectoryInput);
        inputs.add(hookDirectoryInput);
        when(transformInput.getDirectoryInputs()).thenReturn(inputs);
        when(transformInvocation.getInputs()).thenReturn(Collections.singletonList(transformInput));

        // provider
        TransformOutputProvider provider = Mockito.mock(TransformOutputProvider.class);
        when(transformInvocation.getOutputProvider()).thenReturn(provider);
        when(provider.getContentLocation(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(new File(ClassFileUtil.ProductDir));
        return transformInvocation;
    }

    private Map<File, Status> generateHookClassMap(){
        HashMap<File,Status> map = new HashMap<>();
        for (Class hookClass : mHookClass) {
            ClassFileUtil.moveClassToProduce(hookClass);
            map.put(ClassFileUtil.getProductFile(hookClass.getCanonicalName()),Status.ADDED);
        }
        return map;
    }

    public void addHookClass(Class clazz){
        mHookClass.add(clazz);
    }

    private Map<File, Status> generateClassMap(File dir){
        HashMap<File,Status> map = new HashMap<>();
        LinkedList<File> files = new LinkedList<>();
        files.offer(dir);
        while (!files.isEmpty()){
            File file = files.poll();
            if (file.isFile()){
                map.put(file,Status.ADDED);
            }else {
                for (File file1 : file.listFiles()) {
                    files.offer(file1);
                }
            }
        }
        return map;
    }

    @Test
    public void testTransform() throws IOException {
        Project project = Mockito.mock(Project.class);
        when(project.getBuildDir()).thenReturn(new File(ClassFileUtil.MetaDir));
        LancetTransform transform = new LancetTransform(project,new LancetExtension());
        try {
            transform.transform(mockTransformInvocation());
        } catch (TransformException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Process process = Runtime.getRuntime().exec("./../gradlew executeTestSampleProduct");
        BufferedSource source = Okio.buffer(Okio.source(process.getInputStream()));
        String output = source.readUtf8();
        System.out.println("\n"+output);
        source.close();
        checkOutput(output);
        process.destroy();
    }


    public abstract void checkOutput(String output) throws IOException;

}
