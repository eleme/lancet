package me.ele.lancet.weaver.internal.parser;

import com.google.common.base.Strings;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import me.ele.lancet.base.annotations.Call;
import me.ele.lancet.base.annotations.Execute;
import me.ele.lancet.base.annotations.MayCreateSuper;
import me.ele.lancet.base.annotations.NameRegex;
import me.ele.lancet.base.annotations.TargetClass;
import me.ele.lancet.base.annotations.TargetMethod;
import me.ele.lancet.base.annotations.TryCatchHandler;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.meta.ClassMetaInfo;
import me.ele.lancet.weaver.internal.meta.MethodMetaInfo;

/**
 * Created by gengwanpeng on 17/3/21.
 */
public class AnnotationParser {

    private static final String EXECUTE = Type.getType(Execute.class).toString();
    private static final String CALL = Type.getType(Call.class).toString();
    private static final String TRY_CATCH = Type.getType(TryCatchHandler.class).toString();
    private static final String NAME_REGEX = Type.getType(NameRegex.class).toString();
    private static final String TARGET_METHOD = Type.getType(TargetMethod.class).toString();
    private static final String TARGET_CLASS = Type.getType(TargetClass.class).toString();
    private static final String CREATE_SUPER = Type.getType(MayCreateSuper.class).toString();

    private ClassLoader loader;

    public AnnotationParser(ClassLoader loader) {
        this.loader = loader;
    }

    public ClassMetaInfo parse(Class<?> clazz) {
        ClassMetaInfo classMetaInfo = parseClass(clazz);
        ((List<MethodNode>) classMetaInfo.node.methods).stream()
                .map(info -> parseMethods(clazz,info))
                .filter(m -> m != null)
                .forEach(classMetaInfo::addMethod);
        if (classMetaInfo.infos.size() <= 0) {
            return null;
        }
        Log.tag("Parser").w("generate ClassMetaInfo: "+classMetaInfo);
        return classMetaInfo;
    }


    private ClassMetaInfo parseClass(Class<?> clazz) {
        Log.tag("Parser").w("parse aop class: "+clazz);

        ClassMetaInfo classMetaInfo = new ClassMetaInfo(clazz, loader);
        classMetaInfo.myClassName = clazz.getName();
        for (AnnotationNode node : (List<AnnotationNode>) classMetaInfo.node.visibleAnnotations) {
            if (node.desc.equals(TARGET_CLASS) && node.values != null) {
                List values = node.values;
                for (int i = 0; i < values.size(); i += 2) {
                    if ("value".equals(values.get(i))) {
                        classMetaInfo.targetClassName = Strings.nullToEmpty((String) values.get(1 + i));
                    } else if ("superName".equals(values.get(i))) {
                        classMetaInfo.targetSuperClassName = Strings.nullToEmpty((String) values.get(i + 1));
                    } else if ("interfaces".equals(values.get(i))) {
                        List<String> l = (List<String>) values.get(i + 1);
                        if (l != null) {
                            classMetaInfo.targetInterfaces = contentNonNull(l.toArray(new String[l.size()]));
                        }
                    }
                }
            }
        }
        TargetClass targetClass = clazz.getDeclaredAnnotation(TargetClass.class);
        if (targetClass != null) {
            classMetaInfo.targetSuperClassName = Strings.nullToEmpty(targetClass.superName());
            classMetaInfo.targetInterfaces = contentNonNull(targetClass.interfaces());
        }
        return classMetaInfo;
    }

    private static MethodMetaInfo parseMethods(Class<?> clazz,MethodNode method) {
        Log.tag("Parser").w("parse aop method: "+clazz+"."+method.name);

        MethodMetaInfo methodMetaInfo = new MethodMetaInfo();

        if (method.visibleAnnotations != null) {
            for (AnnotationNode node : (List<AnnotationNode>) method.visibleAnnotations) {
                if (node.desc.equals(EXECUTE)) {
                    methodMetaInfo.addType(MethodMetaInfo.TYPE_EXECUTE);
                } else if (node.desc.equals(CALL)) {
                    methodMetaInfo.addType(MethodMetaInfo.TYPE_CALL);
                } else if (node.desc.equals(TRY_CATCH)) {
                    methodMetaInfo.addType(MethodMetaInfo.TYPE_HANDLER);
                } else if (node.desc.equals(NAME_REGEX) && node.values != null) {
                    methodMetaInfo.addNameRegex((String) node.values.get(1));
                } else if (node.desc.equals(TARGET_CLASS) && node.values != null) {
                    List values = node.values;
                    for (int i = 0; i < values.size(); i += 2) {
                        if ("value".equals(values.get(i))) {
                            methodMetaInfo.setTargetClass(Strings.nullToEmpty((String) values.get(1 + i)));
                        } else if ("superName".equals(values.get(i))) {
                            methodMetaInfo.setTargetSuperClass(Strings.nullToEmpty((String) values.get(i + 1)));
                        } else if ("interfaces".equals(values.get(i))) {
                            List<String> l = (List<String>) values.get(i + 1);
                            if (l != null) {
                                methodMetaInfo.setTargetInterfaces(contentNonNull(l.toArray(new String[l.size()])));
                            }
                        }
                    }
                } else if (node.desc.equals(TARGET_METHOD) && node.values != null) {
                    List values = node.values;
                    for (int i = 0; i < values.size(); i += 2) {
                        if ("value".equals(values.get(i))) {
                            methodMetaInfo.setTargetMethod((String) values.get(i + 1));
                        } else if ("isStatic".equals(values.get(i))) {
                            methodMetaInfo.setStatic((Boolean) values.get(i + 1));
                        }
                    }
                } else if (node.desc.equals(CREATE_SUPER)) {
                    methodMetaInfo.setMayCreateSuper(true);
                }
            }
        }

        if (!methodMetaInfo.hasType()) {
            if (!method.name.equals("<init>")){
                Log.w("Method " + clazz+"."+method.desc + " has no aop annotation, such as " +
                        "@Execute @Call @TryCatchHandler.");
            }
            return null;
        }

        methodMetaInfo.setMyDescriptor(method.desc);
        methodMetaInfo.setMyMethod(method.name);
        methodMetaInfo.setNode(method);

        methodMetaInfo.checkMethod();

        return methodMetaInfo;
    }

    private static String[] contentNonNull(String[] interfaces) {
        if (interfaces != null) {
            for (String i : interfaces) {
                if (Strings.isNullOrEmpty(i)) {
                    throw new IllegalArgumentException("name in interfaces can't be null");
                }
            }
        }
        return interfaces;
    }
}
