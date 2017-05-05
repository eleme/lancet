package me.ele.lancet.weaver.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.ele.lancet.base.api.ClassSupplier;
import me.ele.lancet.weaver.ClassData;
import me.ele.lancet.weaver.MetaParser;
import me.ele.lancet.weaver.Weaver;
import me.ele.lancet.weaver.internal.asm.ClassTransform;
import me.ele.lancet.weaver.internal.entity.TotalInfo;
import me.ele.lancet.weaver.internal.graph.Graph;
import me.ele.lancet.weaver.internal.graph.Node;
import me.ele.lancet.weaver.internal.meta.ClassMetaInfo;
import me.ele.lancet.weaver.internal.parser.AsmMetaParser;
import me.ele.lancet.weaver.internal.supplier.ComponentSupplier;
import me.ele.lancet.weaver.internal.supplier.DirCodeSupplier;
import me.ele.lancet.weaver.internal.supplier.JarClassSupplier;


/**
 * Created by gengwanpeng on 17/3/21.
 */
public class AsmWeaver implements Weaver {


    public static Weaver newInstance(ClassLoader cl, Map<String, Node> nodesMap, List<String> classes) {
        MetaParser parser = new AsmMetaParser(cl);
        Graph graph = new Graph(nodesMap);
        return new AsmWeaver(parser.parse(classes, graph));
    }

    private final TotalInfo totalInfo;

    public AsmWeaver(TotalInfo totalInfo) {
        this.totalInfo = totalInfo;
    }


    @Override
    public ClassData[] weave(byte[] input) {
        return ClassTransform.weave(totalInfo, input);
    }

    private static TotalInfo convertToAopInfo(List<ClassMetaInfo> list) {
//        List<ExecuteInfo> executeInfos = list.stream().flatMap(c -> c.infos.stream())
//                .filter(m -> m.getType() == MethodMetaInfo.TYPE_EXECUTE)
//                .map(m -> new ExecuteInfo(m.isStatic(), m.isMayCreateSuper(), m.getTargetClass(), m.getTargetSuperClass(), m.getTargetInterfaces(),
//                        m.getTargetMethod(), m.getMyDescriptor(), m.getMyMethod(), m.getNode()))
//                .collect(Collectors.toList());
//        List<TryCatchInfo> tryCatchInfos = list.stream()
//                .flatMap(c -> c.infos.stream())
//                .filter(m -> m.getType() == MethodMetaInfo.TYPE_HANDLER)
//                .map(m -> new TryCatchInfo(m.getRegex(), m.getMyClass(), m.getMyMethod(), m.getMyDescriptor()))
//                .collect(Collectors.toList());
//
//        List<CallInfo> callInfos = list.stream()
//                .flatMap(c -> c.infos.stream())
//                .filter(m -> m.getType() == MethodMetaInfo.TYPE_CALL)
//                .map(m -> new CallInfo(m.isStatic(), m.getRegex(), m.getTargetClass(), m.getTargetMethod(), m.getMyDescriptor(), m.getMyClass(), m.getMyMethod(), m.getNode()))
//                .peek(CallInfo::transformSelf)
//                .collect(Collectors.toList());
//
//        return new TotalInfo(executeInfos, tryCatchInfos, callInfos);
        return null;
    }

    private static URL[] toUrls(Collection<File> files) {
        return files.stream()
                .map(File::toURI)
                .map(u -> {
                    try {
                        return u.toURL();
                    } catch (MalformedURLException ignored) {
                        throw new AssertionError();
                    }
                })
                .toArray(URL[]::new);
    }


}
