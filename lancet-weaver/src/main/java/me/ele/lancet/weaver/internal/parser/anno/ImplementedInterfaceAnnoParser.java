package me.ele.lancet.weaver.internal.parser.anno;

import com.google.common.base.Strings;
import me.ele.lancet.base.Scope;
import me.ele.lancet.weaver.internal.exception.IllegalAnnotationException;
import me.ele.lancet.weaver.internal.meta.HookInfoLocator;
import me.ele.lancet.weaver.internal.parser.AnnoParser;
import me.ele.lancet.weaver.internal.parser.AnnotationMeta;
import me.ele.lancet.weaver.internal.util.RefHolder;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.*;

/**
 * Created by gengwanpeng on 17/5/3.
 */
public class ImplementedInterfaceAnnoParser implements AnnoParser {

    private static final String ENUM_DESC = Type.getDescriptor(Scope.class);


    @SuppressWarnings("unchecked")
    @Override
    public AnnotationMeta parseAnno(AnnotationNode annotationNode) {
        RefHolder<String[]> interfaces = new RefHolder<>(null);
        RefHolder<Scope> scope = new RefHolder<>(Scope.SELF);

        List<Object> values;
        if ((values = annotationNode.values) != null) {
            for (int i = 0; i < values.size(); i += 2) {
                switch ((String) values.get(i)) {
                    case "value":
                        interfaces.set(contentNonNull(((List<String>) values.get(i + 1)).toArray(new String[0])));
                        break;
                    case "scope":
                        String[] vs = (String[]) values.get(i + 1);
                        if (!ENUM_DESC.equals(vs[0])) {
                            throw new IllegalAnnotationException();
                        }
                        scope.set(Scope.valueOf(vs[1]));
                        break;
                    default:
                        throw new IllegalAnnotationException();
                }
            }

            return new AnnotationMeta(annotationNode.desc) {
                @Override
                public void accept(HookInfoLocator locator) {
                    computeInterface(locator, interfaces.get(), scope.get());
                }
            };
        }

        throw new IllegalAnnotationException("@ImplementedInterface is illegal, must specify value field");
    }

    private static String[] contentNonNull(String[] interfaces) {
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                if (Strings.isNullOrEmpty(interfaces[i])) {
                    throw new IllegalAnnotationException("@ImplementedInterface's value can't be null");
                }
                interfaces[i] = interfaces[i].replace('.', '/');
            }
        }
        return interfaces;
    }

    private void computeInterface(HookInfoLocator locator, String[] interfaces, Scope scope) {


        Arrays.stream(interfaces)
                .forEach(it -> {
                    locator.mayAddCheckFlow(it, scope);
                    Set<String> classes = new HashSet<>();
                    locator.graph()
                            .implementsOf(it, scope)
                            .forEach(node -> {
                                classes.add(node.entity.name);
                            });
                    locator.intersectClasses(classes);
                });
    }
}
