package me.ele.lancet.weaver.internal.asm;


import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import me.ele.lancet.weaver.internal.entity.CallInfo;
import me.ele.lancet.weaver.internal.entity.TotalInfo;
import me.ele.lancet.weaver.internal.log.Impl.SystemOutputImpl;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.util.HookInfoGnenerator;
import me.ele.lancet.weaver.internal.util.TransformHelper;

/**
 * Created by Jude on 2017/4/26.
 */
public class CallStaticTest {

    @Before
    public void setUp(){
        Log.setImpl(new SystemOutputImpl());
    }

    @Test
    public void testTransform() throws IOException {
        TotalInfo totalInfo = new TotalInfo();
        ArrayList<CallInfo> callInfos = new ArrayList<>();
        totalInfo.setCallInfos(callInfos);
        callInfos.addAll(HookInfoGnenerator.callInfoList("com.sample.hook.call.AddSugarHook"));
        System.out.println(callInfos);
        TransformHelper.startTransform(totalInfo);
    }

}