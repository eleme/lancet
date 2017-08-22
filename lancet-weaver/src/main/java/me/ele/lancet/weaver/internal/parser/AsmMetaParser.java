package me.ele.lancet.weaver.internal.parser;

import com.google.common.base.Joiner;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import me.ele.lancet.base.annotations.ClassOf;
import me.ele.lancet.base.annotations.ImplementedInterface;
import me.ele.lancet.base.annotations.Insert;
import me.ele.lancet.base.annotations.NameRegex;
import me.ele.lancet.base.annotations.Proxy;
import me.ele.lancet.base.annotations.TargetClass;
import me.ele.lancet.base.annotations.TryCatchHandler;
import me.ele.lancet.weaver.MetaParser;
import me.ele.lancet.weaver.internal.entity.TransformInfo;
import me.ele.lancet.weaver.internal.exception.LoadClassException;
import me.ele.lancet.weaver.internal.exception.UnsupportedAnnotationException;
import me.ele.lancet.weaver.internal.graph.Graph;
import me.ele.lancet.weaver.internal.log.Log;
import me.ele.lancet.weaver.internal.meta.ClassMetaInfo;
import me.ele.lancet.weaver.internal.meta.MethodMetaInfo;
import me.ele.lancet.weaver.internal.parser.anno.AcceptAny;
import me.ele.lancet.weaver.internal.parser.anno.ClassOfAnnoParser;
import me.ele.lancet.weaver.internal.parser.anno.DelegateAcceptableAnnoParser;
import me.ele.lancet.weaver.internal.parser.anno.GatheredAcceptableAnnoParser;
import me.ele.lancet.weaver.internal.parser.anno.ImplementedInterfaceAnnoParser;
import me.ele.lancet.weaver.internal.parser.anno.InsertAnnoParser;
import me.ele.lancet.weaver.internal.parser.anno.NameRegexAnnoParser;
import me.ele.lancet.weaver.internal.parser.anno.ProxyAnnoParser;
import me.ele.lancet.weaver.internal.parser.anno.TargetClassAnnoParser;
import me.ele.lancet.weaver.internal.parser.anno.TryCatchAnnoParser;


/**
 * Created by gengwanpeng on 17/3/21.
 */
public class AsmMetaParser implements MetaParser {


    private ClassLoader loader;

    public AsmMetaParser(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public TransformInfo parse(List<String> hookClasses, Graph graph) {
        Log.i("aop classes: \n" + hookClasses.stream().collect(Collectors.joining("\n")));

        return hookClasses.stream().map(s -> new AsmClassParser(loader).parse(s))
                .map(c -> c.toLocators(graph))
                .flatMap(Collection::stream)
                .collect(() -> new TransformInfo(hookClasses), (t, l) -> l.appendTo(t), TransformInfo::combine);
    }

    private class AsmClassParser {

        private ClassLoader cl;
        private AcceptableAnnoParser parser;

        public AsmClassParser(ClassLoader loader) {
            this.cl = loader;
            parser = GatheredAcceptableAnnoParser.newInstance(
                    new DelegateAcceptableAnnoParser(Type.getDescriptor(TargetClass.class), new TargetClassAnnoParser()),
                    new DelegateAcceptableAnnoParser(Type.getDescriptor(ImplementedInterface.class), new ImplementedInterfaceAnnoParser()),
                    new DelegateAcceptableAnnoParser(Type.getDescriptor(Insert.class), new InsertAnnoParser()),
                    new DelegateAcceptableAnnoParser(Type.getDescriptor(Proxy.class), new ProxyAnnoParser()),
                    new DelegateAcceptableAnnoParser(Type.getDescriptor(TryCatchHandler.class), new TryCatchAnnoParser()),
                    new DelegateAcceptableAnnoParser(Type.getDescriptor(NameRegex.class), new NameRegexAnnoParser()),
                    new DelegateAcceptableAnnoParser(Type.getDescriptor(ClassOf.class), new ClassOfAnnoParser()),
                    AcceptAny.INSTANCE
            );
        }

        @SuppressWarnings("unchecked")
        public ClassMetaInfo parse(String className) {
            ClassNode cn = loadClassNode(className);
            ClassMetaInfo meta = new ClassMetaInfo(className);
            meta.annotationMetas = nodesToMetas(cn.visibleAnnotations);
            meta.methods = ((List<MethodNode>) cn.methods).stream()
                    .filter(this::checkMethod)
                    .map(mn -> {
                        List<AnnotationMeta> methodMetas = nodesToMetas(mn.visibleAnnotations);

                        MethodMetaInfo mm = new MethodMetaInfo(mn);
                        mm.metaList = methodMetas;

                        if (mn.visibleParameterAnnotations != null) {
                            int size = Arrays.stream(mn.visibleParameterAnnotations)
                                    .filter(Objects::nonNull)
                                    .mapToInt(List::size)
                                    .sum() + methodMetas.size();
                            List<AnnotationMeta> paramAnnoMetas = new ArrayList<>(size);
                            for (int i = 0; i < mn.visibleParameterAnnotations.length; i++) {
                                List<AnnotationNode> list = (List<AnnotationNode>) mn.visibleParameterAnnotations[i];
                                if (list != null) {
                                    for (AnnotationNode a : list) {
                                        a.visit(ClassOf.INDEX, i);
                                    }
                                    paramAnnoMetas.addAll(nodesToMetas(list));
                                }
                            }

                            paramAnnoMetas.addAll(methodMetas);
                            mm.metaList = paramAnnoMetas;
                        }

                        return mm;
                    })
                    .filter(Objects::nonNull).collect(Collectors.toList());

            return meta;
        }

        private List<AnnotationMeta> nodesToMetas(List<AnnotationNode> nodes) {
            if (nodes == null || nodes.size() <= 0) {
                return Collections.emptyList();
            }
            return nodes.stream().map(c -> {
                if (!parser.accept(c.desc)) {
                    throw new UnsupportedAnnotationException(c.desc + " is not supported");
                }
                return parser.parseAnno(c);
            }).collect(Collectors.toList());
        }

        private ClassNode loadClassNode(String className) {
            try {
                URL url = cl.getResource(className + ".class");
                if (url == null) {
                    throw new IOException("url == null");
                }
                URLConnection urlConnection = url.openConnection();

                // gradle daemon bug:
                // Different builds in one process because of daemon which makes the jar connection will read the context from cache if they points to the same jar file.
                // But the file may be changed.

                urlConnection.setUseCaches(false);
                ClassReader cr = new ClassReader(urlConnection.getInputStream());
                urlConnection.getInputStream().close();
                ClassNode cn = new ClassNode();
                cr.accept(cn, ClassReader.SKIP_DEBUG);
                checkNode(cn);
                return cn;
            } catch (IOException e) {
                URLClassLoader cl = (URLClassLoader) this.cl;
                throw new LoadClassException("load class failure: " + className + " by\n" + Joiner.on('\n').join(cl.getURLs()), e);
            }
        }

        @SuppressWarnings("unchecked")
        private void checkNode(ClassNode cn) {
            if (cn.fields.size() > 0) {
                String s = ((List<FieldNode>)cn.fields).stream().map(fieldNode -> fieldNode.name).collect(Collectors.joining(","));
                Log.w("can't declare fields '"+s+"' in hook class "+cn.name);
            }
            int ac = Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC;
            cn.innerClasses.forEach(c -> {
                InnerClassNode n = (InnerClassNode) c;
                if ((n.access & ac) != ac) {
                    throw new IllegalStateException("inner class in hook class "+cn.name+" must be public static");
                }
            });
        }

        private boolean checkMethod(MethodNode methodNode) {
            List<AnnotationNode> list = methodNode.visibleAnnotations;
            if (list != null ){
                return list.stream().anyMatch(annotationNode -> annotationNode.desc.contains("lancet"));
            }
            return false;
        }
    }
}
