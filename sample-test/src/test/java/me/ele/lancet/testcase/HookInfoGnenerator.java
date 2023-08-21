package me.ele.lancet.testcase;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.ele.lancet.plugin.ClassFileUtil;
import me.ele.lancet.weaver.internal.entity.InsertInfo;
import me.ele.lancet.weaver.internal.entity.ProxyInfo;
import me.ele.lancet.weaver.internal.parser.AopMethodAdjuster;
import me.ele.lancet.weaver.internal.util.TypeUtil;
import okio.Okio;

/**
 * Created by Jude on 2017/4/28.
 */

public class HookInfoGnenerator {

    public static List<MethodNode> methodNodeList(String className) throws IOException {
        ClassReader classReader = new ClassReader(Okio.buffer(Okio.source(ClassFileUtil.getClassFile(className))).readByteArray());
        List<MethodNode> hookNode = new ArrayList<>();
        classReader.accept(new ClassVisitor(Opcodes.ASM6) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (name.equals("<init>")){
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
                MethodNode node = new MethodNode(access, name, desc, signature, exceptions);
                hookNode.add(node);
                return new MethodVisitor(Opcodes.ASM6,node) {

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        if (owner.equals("com/sample/hook/Origin")){
                            opcode = AopMethodAdjuster.OP_CALL;
                        }
                        super.visitMethodInsn(opcode, owner, name, desc, itf);
                    }

                };
            }

        },0);
        return hookNode;
    }

    public static List<ProxyInfo> callInfoList(String className) throws IOException {
        List<ProxyInfo> infos = new ArrayList<>();
        for (MethodNode node : methodNodeList(className)) {
            String targetDesc = node.desc;
            if ((node.access & Opcodes.ACC_STATIC) == 0){
                targetDesc = TypeUtil.removeFirstParam(targetDesc);
            }
            AnnotationNode annotationNode = (AnnotationNode) node.visibleAnnotations.get(0);
            infos.add(new ProxyInfo("",(String)annotationNode.values.get(1),node.name,targetDesc,className,node));
        }
        return infos;
    }

    public static List<InsertInfo> executeInfoList(String className) throws IOException {
        List<InsertInfo> infos = new ArrayList<>();
        for (MethodNode node : methodNodeList(className)) {
            String targetDesc = node.desc;
            if ((node.access & Opcodes.ACC_STATIC) == 0){
                targetDesc = TypeUtil.removeFirstParam(targetDesc);
            }
            AnnotationNode annotationNode = (AnnotationNode) node.visibleAnnotations.get(0);
            boolean createSuper = false;
            if (annotationNode.values.size() > 2){
                createSuper = (Boolean) annotationNode.values.get(3);
            }
            infos.add(new InsertInfo(createSuper,(String)annotationNode.values.get(1),node.name,targetDesc,className,node));
        }
        return infos;
    }

}
