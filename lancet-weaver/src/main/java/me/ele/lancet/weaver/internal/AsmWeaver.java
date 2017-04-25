package me.ele.lancet.weaver.internal;

import me.ele.lancet.base.PlaceHolder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import me.ele.lancet.base.api.ClassSupplier;
import me.ele.lancet.weaver.ClassData;
import me.ele.lancet.weaver.MetaParser;
import me.ele.lancet.weaver.Weaver;
import me.ele.lancet.weaver.internal.asm.ClassTransform;
import me.ele.lancet.weaver.internal.asm.CustomClassLoaderClassWriter;
import me.ele.lancet.weaver.internal.asm.classvisitor.CallClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.ExcludeClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.ExecuteClassVisitor;
import me.ele.lancet.weaver.internal.asm.classvisitor.TryCatchInfoClassVisitor;
import me.ele.lancet.weaver.internal.entity.CallInfo;
import me.ele.lancet.weaver.internal.entity.ExecuteInfo;
import me.ele.lancet.weaver.internal.entity.TotalInfo;
import me.ele.lancet.weaver.internal.entity.TryCatchInfo;
import me.ele.lancet.weaver.internal.meta.ClassMetaInfo;
import me.ele.lancet.weaver.internal.meta.MethodMetaInfo;
import me.ele.lancet.weaver.internal.parser.ReflectiveMetaParser;
import me.ele.lancet.weaver.internal.supplier.ComponentSupplier;
import me.ele.lancet.weaver.internal.supplier.DirCodeSupplier;
import me.ele.lancet.weaver.internal.supplier.JarClassSupplier;


/**
 * Created by gengwanpeng on 17/3/21.
 */
public class AsmWeaver implements Weaver {


    public static AsmWeaver newInstance(Collection<File> jars, Collection<File> dirs) {
        URLClassLoader dirLoader = URLClassLoader.newInstance(toUrls(dirs), Thread.currentThread().getContextClassLoader());
        URLClassLoader loader = URLClassLoader.newInstance(toUrls(jars), dirLoader);

        ClassSupplier dirSupplier = new DirCodeSupplier(dirLoader);
        ClassSupplier jarSupplier = new JarClassSupplier(jars, loader);
        ClassSupplier supplier = ComponentSupplier.newInstance(dirSupplier, jarSupplier);

        MetaParser parser = new ReflectiveMetaParser(loader);
        List<Class<?>> classes = supplier.get();
        List<ClassMetaInfo> list = parser.parse(classes);

        return new AsmWeaver(loader, classes, list);
    }

    private final URLClassLoader loader;
    private final List<Class<?>> classes;
    private final TotalInfo totalInfo;

    public AsmWeaver(URLClassLoader loader, List<Class<?>> classes, List<ClassMetaInfo> list) {
        this.loader = loader;
        this.classes = classes;
        this.totalInfo = convertToAopInfo(list);
        List<String> excludes = classes.stream().map(c -> c.getName().replace('.', '/')).collect(Collectors.toList());
        this.totalInfo.setExcludes(excludes);
    }

    @Override
    public List<String> getBuiltInNames() {
        return classes.stream().map(Class::getName).collect(Collectors.toList());
    }

    @Override
    public ClassData[] weave(byte[] input) {
        return ClassTransform.weave(loader,totalInfo,input);
    }

    private static TotalInfo convertToAopInfo(List<ClassMetaInfo> list) {
        List<ExecuteInfo> executeInfos = list.stream().flatMap(c -> c.infos.stream())
                .filter(m -> m.getType() == MethodMetaInfo.TYPE_EXECUTE)
                .map(m -> new ExecuteInfo(m.isStatic(), m.isMayCreateSuper(), m.getTargetClass(), m.getTargetSuperClass(), m.getTargetInterfaces(),
                        m.getTargetMethod(), m.getMyDescriptor(), m.getMyMethod(), m.getNode()))
                .collect(Collectors.toList());
        List<TryCatchInfo> tryCatchInfos = list.stream()
                .flatMap(c -> c.infos.stream())
                .filter(m -> m.getType() == MethodMetaInfo.TYPE_HANDLER)
                .map(m -> new TryCatchInfo(m.getRegex(), m.getMyClass(), m.getMyMethod(), m.getMyDescriptor()))
                .collect(Collectors.toList());

        List<CallInfo> callInfos = list.stream()
                .flatMap(c -> c.infos.stream())
                .filter(m -> m.getType() == MethodMetaInfo.TYPE_CALL)
                .map(m -> new CallInfo(m.isStatic(), m.getRegex(), m.getTargetClass(), m.getTargetMethod(), m.getMyDescriptor(), m.getMyClass(), m.getMyMethod(), m.getNode()))
                .peek(CallInfo::transformSelf)
                .collect(Collectors.toList());

        return new TotalInfo(executeInfos, tryCatchInfos, callInfos);
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
