package me.ele.lancet.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import me.ele.lancet.base.PlaceHolder;
import me.ele.lancet.base.api.ClassSupplier;

/**
 * Created by Jude on 17/4/10.
 */

public class SupplierGenerator {
    Set<ClassName> mAopClasses;

    public SupplierGenerator(Set<ClassName> mAopClasses) {
        this.mAopClasses = mAopClasses;
    }

    public JavaFile build() {
        FieldSpec fieldSpec = FieldSpec.builder(ClassName.get(ClassLoader.class),"classloader")
                .addModifiers(Modifier.PRIVATE)
                .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(ClassLoader.class),"classloader")
                .addStatement("this.classloader = classloader")
                .build();

        MethodSpec.Builder getBuilder = MethodSpec.methodBuilder("get")
                .addAnnotation(ClassName.get(Override.class))
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class),ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class))))
                .addStatement("$T<$T<?>> list = new $T<>()",List.class,Class.class, ArrayList.class)
                .beginControlFlow("try");
        for (ClassName mAopClass : mAopClasses) {
            getBuilder.addStatement("list.add($T.class)",mAopClass);
            getBuilder.addStatement("classloader.loadClass($S)",mAopClass);
        }
        getBuilder.nextControlFlow("catch (ClassNotFoundException e)")
                .addStatement("e.printStackTrace()")
                .endControlFlow();

        getBuilder.addStatement("return list");
        MethodSpec get = getBuilder.build();

        ClassName className = ClassName.bestGuess(PlaceHolder.SUPPLIER_CLASS_NAME);
        TypeSpec typeSpec = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(ClassSupplier.class))
                .addField(fieldSpec)
                .addMethod(constructor)
                .addMethod(get)
                .build();

        return JavaFile.builder(className.packageName(),typeSpec)
                .addFileComment("Generated class from Lancet. Do not modify!")
                .build();
    }
}
