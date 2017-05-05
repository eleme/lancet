package me.ele.lancet.weaver.internal.parser.anno;

import me.ele.lancet.weaver.internal.meta.HookInfoLocator;
import me.ele.lancet.weaver.internal.parser.AnnoParser;
import me.ele.lancet.weaver.internal.parser.AnnotationMeta;
import org.objectweb.asm.tree.AnnotationNode;

/**
 * Created by gengwanpeng on 17/5/5.
 */
public class TryCatchAnnoParser implements AnnoParser {

    @Override
    public AnnotationMeta parseAnno(AnnotationNode annotationNode) {
        return new TryCatchAnnoMeta(annotationNode.desc);
    }

    public class TryCatchAnnoMeta extends AnnotationMeta {


        public TryCatchAnnoMeta(String desc) {
            super(desc);
        }

        @Override
        public void accept(HookInfoLocator locator) {
            locator.setTryCatch();
        }
    }
}
