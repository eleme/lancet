package me.ele.lancet.plugin

import com.android.build.api.transform.TransformException
import me.ele.lancet.weaver.internal.AsmWeaver

class Transformer {
    private static AsmWeaver asmWeaver

    public static void setAsmWeaver(AsmWeaver asmWeaver) {
        this.asmWeaver = asmWeaver
    }

    public static byte[] transform(byte[] classBytes) throws TransformException {

        try {
            asmWeaver.weave(classBytes)
        } catch (e) {
            throw new TransformException(e)
        }
    }
}
