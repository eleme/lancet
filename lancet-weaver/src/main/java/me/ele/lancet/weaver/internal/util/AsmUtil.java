package me.ele.lancet.weaver.internal.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * Created by gengwanpeng on 17/4/11.
 */
public class AsmUtil {

    public static MethodNode clone(MethodNode node) {
        MethodNode clone = new MethodNode(Opcodes.ASM6, node.access, node.name, node.desc, node.signature,
                (String[]) node.exceptions.toArray(new String[node.exceptions.size()]));
        node.accept(clone);
        return clone;
    }
}
