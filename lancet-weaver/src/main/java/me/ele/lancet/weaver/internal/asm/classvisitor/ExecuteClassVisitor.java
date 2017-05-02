package me.ele.lancet.weaver.internal.asm.classvisitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import me.ele.lancet.weaver.internal.asm.ClassTransform;
import me.ele.lancet.weaver.internal.asm.LinkedClassVisitor;
import me.ele.lancet.weaver.internal.entity.ExecuteInfo;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.util.AopMethodAdjuster;
import me.ele.lancet.weaver.internal.util.TypeUtil;

/**
 * Created by Jude on 17/5/2.
 */
public class ExecuteClassVisitor extends LinkedClassVisitor {

    private List<ExecuteInfo> executeInfos;

    private String className;
    private String superClassName;

    private boolean classBingo = false;


    public ExecuteClassVisitor(List<ExecuteInfo> executeInfos) {
        this.executeInfos = executeInfos;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.superClassName = superName;
        if (executeInfos!= null){
            for (ExecuteInfo executeInfo : executeInfos) {
                executeInfo.targetClass = executeInfo.targetClass.replace(".","/");
                if (executeInfo.targetClass.equals(className)){
                    classBingo = true;
                    return;
                }
            }
        }
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (classBingo){
            MethodVisitor tailMV = null;
            MethodNode headMN = null;
            for (int i = 0; i < executeInfos.size(); i++) {
                ExecuteInfo executeInfo = executeInfos.get(i);
                if (executeInfo.targetMethod.equals(name) && executeInfo.targetDesc.equals(desc) && (access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0){
                    Log.tag("transform").i("start weave Execute method: "+className+"."+name);

                    String originName = name+"$"+i;
                    String originDesc = TypeUtil.descToStatic(access,desc,className);
                    if (tailMV == null){
                        // change origin method name.mark as tail of invoke link.
                        int originAccess = access|Opcodes.ACC_STATIC;
                        tailMV = super.visitMethod(originAccess, originName, originDesc, signature, exceptions);
                    }
                    if (headMN != null){
                        // if head exist, push head and create a new head
                        headMN.name = originName;
                        headMN.accept(cv);
                    }
                    headMN = createFakeMethod(access,name,desc,signature,exceptions,i,executeInfo,originName,originDesc);
                    // cancel the createSuper flag if this info has applied.
                    executeInfo.createSuper = false;
                }
            }
            if (tailMV != null){
                headMN.accept(cv);
                return tailMV;
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }


    private MethodNode createFakeMethod(int faccess, String fname, String fdesc, String fsignature, String[] fexceptions, int index, ExecuteInfo executeInfo, String originName,String originDesc){
        // all visitor will share the only one innerclass
        ClassWriter writer = getClassCollector().getInnerClassWriter(ClassTransform.AID_INNER_CLASS_NAME);

        String innerClassName = getClassCollector().getCanonicalName(ClassTransform.AID_INNER_CLASS_NAME);
        // every method in innerclass will add hook class's name as prefix. to show invoke path when print error stack and debug.
        String methodName = executeInfo.sourceClass.replace(".","_")+"_"+fname;

        MethodNode proxyMethod = new MethodNode(Opcodes.ASM5, Opcodes.ACC_STATIC, methodName, executeInfo.sourceMethod.desc, executeInfo.sourceMethod.signature, executeInfo.sourceMethod.exceptions.toArray(new String[executeInfo.sourceMethod.exceptions.size()]));
        // copy the code from hook method to inner class and replace the "Origin.call"
        executeInfo.sourceMethod.accept(new MethodVisitor(Opcodes.ASM5,proxyMethod) {

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

                if (opcode == AopMethodAdjuster.OP_FLAG){

                    opcode = Opcodes.INVOKESTATIC;
                    owner = className;
                    name = originName;
                    desc = originDesc;

                    // load all arguments and then invoke real origin method.
                    Type[] types = Type.getMethodType(desc).getArgumentTypes();
                    int index = 0;
                    for (int i = 0; i < types.length; i++) {
                        super.visitVarInsn(types[i].getOpcode(Opcodes.ILOAD),index);
                        index += types[i].getSize();
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc, false);
            }

            /**
             * override this method to delete 'this' var in method when origin method is nor static.
             * 'this' var is always at index 0 and length is 1.
             * So minus 1 when origin method is not static.
             */
            @Override
            public void visitVarInsn(int opcode, int var) {
                if ((executeInfo.sourceMethod.access & Opcodes.ACC_STATIC)==0){
                    var--;
                }
                super.visitVarInsn(opcode, var);
            }

            /**
             * edit the LocalVariable to delete 'this' var when origin method is nor static.
             */
            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                if ((executeInfo.sourceMethod.access & Opcodes.ACC_STATIC)==0){
                    if (name.equals("this")){
                        return;
                    }else {
                        index--;
                    }
                }
                super.visitLocalVariable(name, desc, signature, start, end, index);
            }
        });

        proxyMethod.accept(writer);


        // because the target method has renamed,so there will generate a fake method use origin method name.
        final MethodNode fakeMethod = new MethodNode(Opcodes.ASM5,faccess,fname,fdesc,fsignature,fexceptions);
        GeneratorAdapter adapter = new GeneratorAdapter(faccess, new Method(fname, fdesc), fakeMethod);
        if ((faccess & Opcodes.ACC_STATIC)==0){
            adapter.loadThis();
        }
        adapter.loadArgs();
        adapter.invokeStatic(Type.getObjectType(innerClassName),new Method(proxyMethod.name,proxyMethod.desc));
        adapter.returnValue();

        // count the stack&local
        int stack = Type.getArgumentsAndReturnSizes(fdesc) >> 2;
        int local = (stack == 1 && (fdesc.endsWith("D") || fdesc.endsWith("J"))) ? 2 : stack;
        adapter.visitMaxs(stack, local);
        adapter.visitEnd();

        // return the fake origin method but accept the method, because we may rename this method again.
        return fakeMethod;
    }

    @Override
    public void visitEnd() {
        if (classBingo){
            for (ExecuteInfo executeInfo : executeInfos) {
                if (executeInfo.targetClass.equals(className) && executeInfo.createSuper){
                    Log.tag("transform").i("start weave Execute method: "+className+"."+executeInfo.targetMethod);

                    MethodVisitor mv = super.visitMethod(
                            executeInfo.sourceMethod.access,
                            executeInfo.targetMethod,
                            executeInfo.targetDesc,
                            executeInfo.sourceMethod.signature,
                            executeInfo.sourceMethod.exceptions.toArray(new String[0])
                    );
                    // copy name from hook class to target class and replace the "Origin.call"
                    executeInfo.sourceMethod.accept(new MethodVisitor(Opcodes.ASM5,mv) {

                        @Override
                        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                            return null;
                        }

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

                            if (opcode == AopMethodAdjuster.OP_FLAG){
                                // load this
                                super.visitVarInsn(Opcodes.ALOAD,0);
                                // invoke super's method
                                opcode = Opcodes.INVOKESPECIAL;
                                owner = superClassName;
                                name = executeInfo.targetMethod;
                                desc = executeInfo.targetDesc;

                                Type[] types = Type.getMethodType(desc).getArgumentTypes();
                                int index = 0;
                                for (int i = 0; i < types.length; i++) {
                                    super.visitVarInsn(types[i].getOpcode(Opcodes.ILOAD),index);
                                    index += types[i].getSize();
                                }
                            }
                            super.visitMethodInsn(opcode, owner, name, desc, false);
                        }
                    });
                }
            }
        }
        super.visitEnd();
    }

}
