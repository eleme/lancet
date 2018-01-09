package me.ele.lancet.weaver.internal.meta;

import com.google.common.collect.Sets;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.Set;

import me.ele.lancet.base.Scope;
import me.ele.lancet.weaver.internal.entity.ProxyInfo;
import me.ele.lancet.weaver.internal.entity.InsertInfo;
import me.ele.lancet.weaver.internal.entity.TransformInfo;
import me.ele.lancet.weaver.internal.entity.TryCatchInfo;
import me.ele.lancet.weaver.internal.exception.IllegalAnnotationException;
import me.ele.lancet.weaver.internal.graph.ClassNode;
import me.ele.lancet.weaver.internal.graph.Graph;
import me.ele.lancet.weaver.internal.graph.InterfaceNode;
import me.ele.lancet.weaver.internal.graph.Node;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.parser.AopMethodAdjuster;
import me.ele.lancet.weaver.internal.util.TypeUtil;

/**
 * Created by gengwanpeng on 17/5/3.
 */
public class HookInfoLocator {

    private static final int INSERT = 1;
    private static final int PROXY = 2;
    private static final int TRY_CATCH = 4;

    private static final int PUBLIC_STATIC = Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC;

    private int flag = 0;

    private Set<String> classes;
    private Set<String> tempClasses;

    private String targetDesc;
    private String targetMethod;
    private boolean mayCreateSuper;
    private String nameRegex;

    private Type[] argsType;
    private Type returnType;

    private MethodNode sourceNode;
    private String sourceClass;

    private String flowClassName;
    private final Graph graph;
    private boolean shouldIgnoreCheck;

    public HookInfoLocator(Graph graph) {
        this.graph = graph;
    }

    public Graph graph() {
        return graph;
    }

    public Type[] getArgsType() {
        return argsType;
    }

    public void goMethod() {
        tempClasses = null;
    }

    public void intersectClasses(Set<String> classes) {
        if (tempClasses == null) {
            this.tempClasses = classes;
        } else {
            tempClasses = Sets.intersection(tempClasses, classes);
        }
        this.classes = tempClasses;
    }

    public void adjustTargetMethodArgs(int index, Type type) {
        argsType[index] = type;
        targetDesc = sourceNode.desc = Type.getMethodDescriptor(returnType, argsType);
    }

    public void setInsert(String targetMethod, boolean mayCreateSuper, boolean shouldIgnoreCheck) {
        this.flag |= INSERT;
        this.targetMethod = targetMethod;
        this.mayCreateSuper = mayCreateSuper;
        this.shouldIgnoreCheck = shouldIgnoreCheck;
    }

    public void setProxy(String targetMethod) {
        this.flag |= PROXY;
        this.targetMethod = targetMethod;
        if (flowClassName != null) {
            this.graph.flow()
                    .exactlyMatch(flowClassName);
        }
    }

    public void setTryCatch() {
        this.flag |= TRY_CATCH;
    }

    public void setNameRegex(String regex) {
        this.nameRegex = regex;
    }

    public void setSourceNode(String sourceClass, MethodNode node) {
        this.sourceClass = sourceClass;
        this.sourceNode = node;

        targetDesc = sourceNode.desc;

        argsType = Type.getArgumentTypes(targetDesc);
        returnType = Type.getReturnType(targetDesc);
    }

    public void appendTo(TransformInfo transformInfo) {
        check();
        switch (flag) {
            case INSERT:
                classes.stream()
                        .map(c -> new InsertInfo(mayCreateSuper, c, targetMethod, targetDesc, sourceClass, sourceNode, shouldIgnoreCheck))
                        .forEach(transformInfo::addInsertInfo);
                break;
            case PROXY:
                classes.stream()
                        .map(c -> new ProxyInfo(nameRegex, c, targetMethod, targetDesc, sourceClass, sourceNode))
                        .forEach(transformInfo::addProxyInfo);
                break;
            case TRY_CATCH:
                transformInfo.addTryCatch(new TryCatchInfo(nameRegex, sourceClass, sourceNode.name, targetDesc));
                break;
        }
    }

    private void check() {
        if (flag <= 0) {
            throw new IllegalAnnotationException("no @Proxy, @Insert or @TryCatchHandler on " + sourceClass + "." + sourceNode.name);
        } else if (Integer.bitCount(flag) > 1) {
            throw new IllegalAnnotationException("@Proxy @Insert or @TryCatchHandler can only appear once");
        }
        if (flag != TRY_CATCH) {
            if (classes == null) {
                throw new IllegalAnnotationException("no @targetClass or @ImplementedInterface on " + sourceClass + "." + sourceNode.name);
            }
            if (classes.size() <= 0) {
                Log.w("can't find satisfied class with " + sourceClass + "." + sourceNode.name);
            }
        } else {
            if (!targetDesc.equals("(Ljava/lang/Throwable;)Ljava/lang/Throwable;") ||
                    (sourceNode.access & PUBLIC_STATIC) != PUBLIC_STATIC) {
                throw new IllegalAnnotationException("method annotated with @TryCatchHandler should be like this: " +
                        "public static Throwable method_name(Throwable)");
            }
        }
        if (mayCreateSuper && TypeUtil.isStatic(sourceNode.access)) {
            throw new IllegalAnnotationException("can't use mayCreateSuper while method is static, " + sourceClass + "." + sourceNode.name);
        }
    }

    public boolean satisfied() {
        return classes != null && classes.size() > 0 && (flag == INSERT || flag == PROXY);
    }

    public void transformNode() {
        new AopMethodAdjuster(flag == INSERT, sourceClass, sourceNode).adjust();
    }

    public void mayAddCheckFlow(String className, Scope scope) {
        Node node = graph.get(className);
        if (node instanceof ClassNode) {
            if (scope == Scope.ALL || scope == Scope.LEAF) {
                this.flowClassName = className;
                graph.flow().add(graph, className, scope);
            }
        } else if (node instanceof InterfaceNode) {
            if (scope != Scope.DIRECT) {
                this.flowClassName = className;
                graph.flow().add(graph, className, scope);
            }
        }
    }

    @Override
    public String toString() {
        return "HookInfoLocator{" +
                "flag=" + flag +
                ", classes=" + classes +
                ", targetDesc='" + targetDesc + '\'' +
                ", targetMethod='" + targetMethod + '\'' +
                ", mayCreateSuper=" + mayCreateSuper +
                ", nameRegex='" + nameRegex + '\'' +
                ", argsType=" + Arrays.toString(argsType) +
                ", returnType=" + returnType +
                ", sourceClass='" + sourceClass + '\'' +
                '}';
    }
}
