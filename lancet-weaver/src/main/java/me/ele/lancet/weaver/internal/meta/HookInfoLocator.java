package me.ele.lancet.weaver.internal.meta;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import me.ele.lancet.weaver.internal.entity.CallInfo;
import me.ele.lancet.weaver.internal.entity.ExecuteInfo;
import me.ele.lancet.weaver.internal.entity.TotalInfo;
import me.ele.lancet.weaver.internal.entity.TryCatchInfo;
import me.ele.lancet.weaver.internal.exception.IllegalAnnotationException;
import me.ele.lancet.weaver.internal.graph.Graph;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.util.AopMethodAdjuster;
import me.ele.lancet.weaver.internal.util.AsmUtil;
import me.ele.lancet.weaver.internal.util.CollectionUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * Created by gengwanpeng on 17/5/3.
 */
public class HookInfoLocator {

    private static final int INSERT = 0;
    private static final int PROXY = 1;
    private static final int TRY_CATCH = 2;

    private int flag = -1;

    private List<String> classes;
    private List<String> tempClasses;

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

    public void intersectClasses(List<String> classes) {
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
        this.flag = INSERT;
        this.targetMethod = targetMethod;
        this.mayCreateSuper = mayCreateSuper;
    }

    public void setProxy(String targetMethod) {
        this.flag = PROXY;
        this.targetMethod = targetMethod;
    }

    public void setTryCatch() {
        this.flag = TRY_CATCH;
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
        if (flag < 0) {
            throw new IllegalAnnotationException("no @Proxy, @Insert or @TryCatchHandler on " + sourceClass + "." + sourceNode.name);
        }
        if (classes.size() <= 0) {
            Log.w("can't find satisfied class with " + sourceClass + "." + sourceNode.name);
        }

        if (mayCreateSuper && AsmUtil.isStatic(sourceNode.access)) {
            throw new IllegalAnnotationException("can't use mayCreateSuper while method is static, " + sourceClass + "." + sourceNode.name);
        }
    }

    public boolean satisfied() {
        return classes != null && classes.size() > 0 && (flag == INSERT || flag == PROXY);
    }

    public void transformNode() {
        new AopMethodAdjuster(sourceClass, sourceNode).adjust();
    }
}
