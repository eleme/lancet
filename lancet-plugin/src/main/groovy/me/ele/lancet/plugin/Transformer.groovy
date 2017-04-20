package me.ele.lancet.plugin

import com.android.build.api.transform.TransformException
import me.ele.lancet.weaver.Weaver
import me.ele.lancet.weaver.internal.AsmWeaver

class Transformer {
    private static Weaver sWeaver

    static void setWeaver(Weaver weaver) {
        sWeaver = weaver
    }

    static byte[] transform(byte[] classBytes) throws TransformException {

        try {
            sWeaver.weave(classBytes)
        } catch (e) {
            throw new TransformException(e)
        }
    }
}
