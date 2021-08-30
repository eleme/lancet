package me.ele.lancet.weaver.internal.asm;

import com.google.common.base.Preconditions;
import me.ele.lancet.base.annotations.ClassOf;
import me.ele.lancet.weaver.internal.asm.classvisitor.methodvisitor.AutoUnboxMethodVisitor;
import me.ele.lancet.weaver.internal.graph.ClassEntity;
import me.ele.lancet.weaver.internal.graph.FieldEntity;
import me.ele.lancet.weaver.internal.graph.Graph;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.parser.AopMethodAdjuster;
import me.ele.lancet.weaver.internal.util.Bitset;
import me.ele.lancet.weaver.internal.util.PrimitiveUtil;
import me.ele.lancet.weaver.internal.util.TypeUtil;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Created by gengwanpeng on 17/5/9.
 */
public class MethodChain {

    private static final String ACCESS = "access$";
    private static final String FORMAT = "access$%03d";
    private static final String CLASS_OF = Type.getDescriptor(ClassOf.class);

    private final String className;
    private final ClassVisitor base;
    private final Graph graph;
    private Bitset bitset;

    private Invoker head;

    private Map<String, FieldEntity> fieldMap;
    private Map<String, Invoker> invokerMap = new HashMap<>();


    public MethodChain(String className, ClassVisitor base, Graph graph) {
        this.className = className;
        this.base = base;
        this.graph = graph;
        this.bitset = new Bitset();
        this.bitset.setInitializer(b -> {
            int len = ACCESS.length();
            ClassEntity entity = graph.get(className).entity;
            entity.methods.forEach(m -> {
                if (TypeUtil.isStatic(m.access) && m.name.startsWith(ACCESS)) {
                    bitset.tryAdd(m.name, len);
                }
            });
        });
    }

    private void head(int access, int opcode, String owner, String name, String desc) {
        this.head = Invoker.forMethod(
                new MethodInsnNode(opcode, owner, name, desc, opcode == Opcodes.INVOKEINTERFACE)
                , !hasPermission(access, owner), className);
    }

    private boolean hasPermission(int access, String owner) {
        return TypeUtil.isPublic(access) || !TypeUtil.isPrivate(access) && owner.equals(className);
    }

    public void headFromProxy(int opcode, String owner, String name, String desc) {
        int access = Opcodes.ACC_PRIVATE;
        if (opcode == Opcodes.INVOKEINTERFACE || opcode == Opcodes.INVOKEVIRTUAL) {
            access = Opcodes.ACC_PUBLIC;
        }
        head(access, opcode, owner, name, desc);
    }

    public void headFromInsert(int access, String owner, String name, String desc) {
        head(access, TypeUtil.isStatic(access) ? Opcodes.INVOKESTATIC : Opcodes.INVOKESPECIAL, owner, name, desc);
    }


    public void next(String className, int access, String name, String desc, MethodNode node, ClassVisitor cv) {
        String[] exs = (String[]) node.exceptions.toArray(new String[0]);
        head.createIfNeed(base, bitset, exs);

        MethodVisitor mv = cv.visitMethod(access, name, desc, null, exs);
        node.accept(new MethodVisitor(Opcodes.ASM6, new AutoUnboxMethodVisitor(mv)) {

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                if (opcode == AopMethodAdjuster.OP_CALL) {
                    head.loadArgsAndInvoke(mv);
                } else if (opcode == AopMethodAdjuster.OP_THIS_GET_FIELD) {
                    dealField(Opcodes.GETFIELD, name, mv);
                } else if (opcode == AopMethodAdjuster.OP_THIS_PUT_FIELD) {
                    dealField(Opcodes.PUTFIELD, name, mv);
                } else {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            }

            @Override
            public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
                if (CLASS_OF.equals(desc)) {
                    return null;
                }
                return super.visitParameterAnnotation(parameter, desc, visible);
            }

        });

        headFromInsert(access, className, name, desc);
    }

    private void dealField(int opcode, String name, MethodVisitor mv) {
        initFields();

        // always store in object, auto box and unbox.
        final String obj = "Ljava/lang/Object;";

        FieldEntity entity = fieldMap.get(name);
        if (entity == null) {
            base.visitField(Opcodes.ACC_PRIVATE, name, obj, null, null);
            fieldMap.put(name, entity = new FieldEntity(Opcodes.ACC_PRIVATE, name, obj));
        }

        boolean needCreate = TypeUtil.isPrivate(entity.access);
        String desc = entity.desc;

        invokerMap.computeIfAbsent(opcode + " " + name,
                k -> {
                    Invoker invoker = Invoker.forField(new FieldInsnNode(opcode, className, name, desc), needCreate, className);
                    invoker.createIfNeed(base, bitset, null);
                    return invoker;
                })
                .loadArgsAndInvoke(mv);

    }

    private void initFields() {
        if (fieldMap == null) {
            this.fieldMap = graph.get(className).entity.fields.stream()
                    .collect(Collectors.toMap(f -> f.name, f -> f));
        }
    }

    public void fakePreMethod(String className, int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = base.visitMethod(access, name, desc, null, exceptions);

        createMethod(access, desc, head.action()).accept(mv);

        headFromInsert(access, className, name, desc);
    }

    public Invoker getHead(){
        return head;
    }

    public void visitHead(MethodVisitor mv) {
        head.invoke(mv);
    }

    public static class Invoker implements Opcodes {

        public static Invoker forField(FieldInsnNode fn, boolean needCreate, String className) {
            String staticDesc = staticDesc(className, null, Preconditions.checkNotNull(fn));
            return new Invoker(null, fn, needCreate, staticDesc, className);
        }

        public static Invoker forMethod(MethodInsnNode mn, boolean needCreate, String className) {
            String staticDesc = staticDesc(mn.owner, Preconditions.checkNotNull(mn), null);
            return new Invoker(mn, null, needCreate, staticDesc, className);
        }

        private static String staticDesc(String className, MethodInsnNode mn, FieldInsnNode fn) {
            String desc = mn != null ?
                    mn.desc :
                    (fn.getOpcode() == PUTFIELD ?
                            '(' + fn.desc + ")V" : "()" + fn.desc);
            int access = mn != null && mn.getOpcode() == INVOKESTATIC ? ACC_STATIC : 0;
            return TypeUtil.descToStatic(access, desc, className);
        }

        final MethodInsnNode mn;
        final FieldInsnNode fn;

        final String staticDesc;
        final String owner;
        final boolean needCreate;

        MethodInsnNode syntheticNode;

        Invoker(MethodInsnNode mn, FieldInsnNode fn, boolean needCreate, String staticDesc, String owner) {
            this.mn = mn;
            this.fn = fn;
            this.needCreate = needCreate;
            this.staticDesc = staticDesc;
            this.owner = owner;
        }

        public void createIfNeed(ClassVisitor cv, Bitset bitset, String[] exceptions) {
            if (syntheticNode != null) {
                throw new IllegalStateException("can't create more than once");
            }
            if (needCreate) {
                String name = String.format(FORMAT, bitset.consume());

                syntheticNode = new MethodInsnNode(INVOKESTATIC, owner, name, staticDesc, false);
                Log.tag("transform").i("create synthetic node :" + owner + " " + name + " " + staticDesc);

                MethodVisitor mv = cv.visitMethod(ACC_STATIC | ACC_SYNTHETIC, name, staticDesc, null, exceptions);

                createMethod(ACC_STATIC, staticDesc, mn == null ? fn : mn)
                        .accept(mv);
            }
        }

        public void invoke(MethodVisitor mv) {
            action().accept(mv);
        }

        public void loadArgsAndInvoke(MethodVisitor mv) {
            //load args
            if (mn != null) {
                Type[] params = Type.getArgumentTypes(staticDesc);
                int index = 0;
                for (Type t : params) {
                    mv.visitVarInsn(t.getOpcode(ILOAD), index);
                    index += t.getSize();
                }
                invoke(mv);
            } else {
                if (fn.getOpcode() == PUTFIELD) { // unbox
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitInsn(SWAP);
                    if (PrimitiveUtil.isPrimitive(fn.desc)) {
                        String owner = PrimitiveUtil.box(fn.desc);
                        mv.visitMethodInsn(INVOKEVIRTUAL, PrimitiveUtil.virtualType(owner),
                                PrimitiveUtil.unboxMethod(owner), "()" + fn.desc, false);
                    }
                    invoke(mv);
                } else { // box
                    mv.visitVarInsn(ALOAD, 0);
                    invoke(mv);
                    if (PrimitiveUtil.isPrimitive(fn.desc)) {
                        String owner = PrimitiveUtil.box(fn.desc);
                        mv.visitMethodInsn(INVOKESTATIC, owner,
                                "valueOf", "(" + fn.desc + ")L" + owner + ";", false);
                        ((AutoUnboxMethodVisitor) mv).markBoxed();
                    }
                }
            }
        }

        public AbstractInsnNode action() {
            if (syntheticNode != null) {
                return syntheticNode;
            } else if (mn != null) {
                return mn;
            } else {
                return fn;
            }
        }
    }

    private static Consumer<MethodVisitor> createMethod(int access, String desc, AbstractInsnNode action) {
        return mv -> {
            mv.visitCode();

            //load args
            Type[] params = Type.getArgumentTypes(desc);
            int index = 0;
            if (!TypeUtil.isStatic(access)) {
                index++;
                mv.visitVarInsn(Opcodes.ALOAD, 0);
            }

            for (Type t : params) {
                mv.visitVarInsn(t.getOpcode(Opcodes.ILOAD), index);
                index += t.getSize();
            }
            // action
            action.accept(mv);

            // ret
            Type ret = Type.getReturnType(desc);
            mv.visitInsn(ret.getOpcode(Opcodes.IRETURN));

            mv.visitMaxs(Math.max(index, ret.getSize()), index);
            mv.visitEnd();
        };
    }
}
