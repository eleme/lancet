package me.ele.lancet.weaver.internal.meta;

import me.ele.lancet.weaver.internal.entity.CallInfo;
import me.ele.lancet.weaver.internal.entity.ExecuteInfo;
import me.ele.lancet.weaver.internal.entity.TotalInfo;
import me.ele.lancet.weaver.internal.entity.TryCatchInfo;
import me.ele.lancet.weaver.internal.exception.IllegalAnnotationException;
import me.ele.lancet.weaver.internal.graph.Graph;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.parser.AopMethodAdjuster;
import me.ele.lancet.weaver.internal.util.CollectionUtil;
import me.ele.lancet.weaver.internal.util.TypeUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.List;

/**
 * Created by gengwanpeng on 17/5/3.
 */
public class HookInfoLocator {

    private static final int INSERT = 1;
    private static final int PROXY = 2;
    private static final int TRY_CATCH = 4;

    private int flag = 0;

    private Collection<String> classes;
    private Collection<String> tempClasses;

    private String targetDesc;
    private String targetMethod;
    private boolean mayCreateSuper;
    private String nameRegex;

    private Type[] argsType;
    private Type returnType;

    private MethodNode sourceNode;
    private String sourceClass;


    private final Graph graph;

    public HookInfoLocator(Graph graph) {
        this.graph = graph;
    }

    public Graph graphs() {
        return graph;
    }

    public Type[] getArgsType() {
        return argsType;
    }

    public void goMethod() {
        tempClasses = null;
    }

    public void intersectClasses(Collection<String> classes) {
        if (tempClasses == null) {
            this.tempClasses = classes;
        } else {
            tempClasses = CollectionUtil.intersection(tempClasses, classes);
        }
        this.classes = tempClasses;
    }

    public void adjustTargetMethodArgs(int index, Type type) {
        argsType[index] = type;
        targetDesc = sourceNode.desc = Type.getMethodDescriptor(returnType, argsType);
    }

    public void setInsert(String targetMethod, boolean mayCreateSuper) {
        this.flag |= INSERT;
        this.targetMethod = targetMethod;
        this.mayCreateSuper = mayCreateSuper;
    }

    public void setProxy(String targetMethod) {
        this.flag |= PROXY;
        this.targetMethod = targetMethod;
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

    public void appendTo(TotalInfo totalInfo) {
        check();
        if (classes.size() > 0) {
            switch (flag) {
                case INSERT:
                    classes.stream()
                            .map(c -> new ExecuteInfo(mayCreateSuper, c, targetMethod, targetDesc, sourceClass, sourceNode))
                            .forEach(totalInfo::addExecute);
                    break;
                case PROXY:
                    classes.stream()
                            .map(c -> new CallInfo(nameRegex, c, targetMethod, targetDesc, sourceClass, sourceNode))
                            .forEach(totalInfo::addCall);
                    break;
                case TRY_CATCH:
                    classes.stream()
                            .map(c -> new TryCatchInfo(nameRegex, sourceClass, sourceNode.name, targetDesc))
                            .forEach(totalInfo::addTryCatch);
                    break;
            }
        }
    }

    private void check() {
        if (classes == null) {
            throw new IllegalAnnotationException("no @targetClass or @ImplementedInterface on " + sourceClass + "." + sourceNode.name);
        }
        if (flag <= 0) {
            throw new IllegalAnnotationException("no @Proxy, @Insert or @TryCatchHandler on " + sourceClass + "." + sourceNode.name);
        }else if(Integer.bitCount(flag) > 1){
            throw new IllegalAnnotationException("@Proxy @Insert or @TryCatchHandler can only appear once");
        }
        if (classes.size() <= 0) {
            Log.w("can't find satisfied class with " + sourceClass + "." + sourceNode.name);
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
}
