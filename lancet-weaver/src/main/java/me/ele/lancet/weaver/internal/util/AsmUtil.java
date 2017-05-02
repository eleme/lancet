package me.ele.lancet.weaver.internal.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * Created by gengwanpeng on 17/4/11.
 */
public class AsmUtil {

    public static MethodNode clone(MethodNode node) {
        MethodNode clone = new MethodNode(Opcodes.ASM5, node.access, node.name, node.desc, node.signature,
                (String[]) node.exceptions.toArray(new String[node.exceptions.size()]));
        node.accept(clone);
        return clone;
    }

    public static int getVarInsnOpCode(Type type){
        if (type == Type.BYTE_TYPE
                || type == Type.BOOLEAN_TYPE
                || type == Type.CHAR_TYPE
                || type == Type.SHORT_TYPE
                || type == Type.INT_TYPE){
            return Opcodes.ILOAD;
        }else if (type == Type.LONG_TYPE){
            return Opcodes.LLOAD;
        }else if (type == Type.FLOAT_TYPE){
            return Opcodes.FLOAD;
        }else if (type == Type.DOUBLE_TYPE){
            return Opcodes.DLOAD;
        }else {
            return Opcodes.ALOAD;
        }
    }

}
