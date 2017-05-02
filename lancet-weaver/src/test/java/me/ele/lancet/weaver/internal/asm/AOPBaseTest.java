package me.ele.lancet.weaver.internal.asm;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import me.ele.lancet.weaver.internal.entity.TotalInfo;
import me.ele.lancet.weaver.internal.log.Impl.SystemOutputImpl;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.util.ClassFileUtil;
import me.ele.lancet.weaver.internal.util.HookInfoGnenerator;
import me.ele.lancet.weaver.internal.util.TransformHelper;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by Jude on 2017/5/2.
 */

public abstract class AOPBaseTest {
    TotalInfo totalInfo;

    @Before
    public void setUp(){
        Log.setImpl(new SystemOutputImpl());
        ClassFileUtil.resetProductDir();
    }

    @Test
    public void testTransform() throws IOException {
        totalInfo = new TotalInfo();
        totalInfo.setCallInfos(new ArrayList<>());
        totalInfo.setExecuteInfos(new ArrayList<>());
        totalInfo.setTryCatchInfos(new ArrayList<>());
        totalInfo.setExcludes(new ArrayList<>());
        applyTotalInfo();
        System.out.println(totalInfo);
        TransformHelper.startTransform(totalInfo);
        Process process = Runtime.getRuntime().exec("./gradlew lancet-weaver:executeTestSampleProduct");
        BufferedSource source = Okio.buffer(Okio.source(process.getInputStream()));
        String output = source.readUtf8();
        System.out.println("\n"+output);
        source.close();
        checkOutput(output);
        process.destroy();
    }

    public abstract void applyTotalInfo() throws IOException;

    public abstract void checkOutput(String output) throws IOException;

    public void addCallClass(String className) throws IOException {
        totalInfo.callInfos.addAll(HookInfoGnenerator.callInfoList(className));
    }

    public void addExecuteClass(String className) throws IOException {
        totalInfo.executeInfos.addAll(HookInfoGnenerator.executeInfoList(className));
    }
}
