package me.ele.lancet.weaver.internal.meta;

import me.ele.lancet.weaver.internal.entity.TotalInfo;
import me.ele.lancet.weaver.internal.graph.Graph;
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
            // TODO join temp and parameter
        }
        this.classes = tempClasses;
    }

    public void locateMethodAndType(String targetMethod, int hookType) {
        this.targetMethod = targetMethod;
    }

    public void adjustTargetMethodArgs(int index, Type type) {
        argsType[index] = type;
        targetDesc = sourceNode.desc = Type.getMethodDescriptor(returnType, argsType);
    }

    public void setInsert(String targetMethod, boolean mayCreateSuper){
        this.flag = INSERT;
        this.targetMethod = targetMethod;
        this.mayCreateSuper = mayCreateSuper;
    }

    public void setProxy(String targetMethod){
        this.flag = PROXY;
        this.targetMethod = targetMethod;
    }

    public void setTryCatch(){
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
        // TODO append
    }

    private void check() {

    }
}
