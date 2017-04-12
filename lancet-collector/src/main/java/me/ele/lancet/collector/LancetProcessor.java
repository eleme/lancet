package me.ele.lancet.collector;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import me.ele.lancet.base.annotations.TargetClass;


/**
 * To generator the real DirClassSupplier.Provide the class's names which declare the aop info.
 */
@AutoService(Processor.class)
public class LancetProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;

    Set<ClassName> mAopClasses;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(TargetClass.class);
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
        mAopClasses = new HashSet<>();
        for (Element element : env.getElementsAnnotatedWith(TargetClass.class)) {
            TypeElement enclosingElement;
            if (element instanceof TypeElement){
                enclosingElement = (TypeElement) element;
            }else {
                enclosingElement = (TypeElement) element.getEnclosingElement();
            }
            mAopClasses.add(ClassName.bestGuess(enclosingElement.getQualifiedName().toString()));
        }
        try {
            new SupplierGenerator(mAopClasses).build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
