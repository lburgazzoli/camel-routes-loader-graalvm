package org.apache.camel.graalvm.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Converter;
import org.apache.camel.Endpoint;
import org.apache.camel.Producer;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.builder.ExpressionClause;
import org.apache.camel.component.bean.BeanConverterLoader;
import org.apache.camel.component.file.strategy.GenericFileProcessStrategyFactory;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.SetBodyDefinition;
import org.apache.camel.model.SetHeaderDefinition;
import org.apache.camel.model.ToDefinition;
import org.apache.camel.model.language.ExpressionDefinition;
import org.apache.camel.model.language.LanguageExpression;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.ExchangeFormatter;
import org.apache.camel.spi.Language;
import org.apache.camel.spi.TypeConverterLoader;
import org.apache.xbean.finder.ClassFinder;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

public class CamelFeature implements Feature {

    private static void allowInstantiate(Class cl) {
        RuntimeReflection.register(cl);
        for (Constructor<?> c : cl.getConstructors()) {
            RuntimeReflection.register(c);
        }
    }

    private static void allowMethods(Class cl) {
        for (Method method : cl.getMethods()) {
            RuntimeReflection.register(method);
        }
    }

    private static void allowAll(Class cl) {
        allowInstantiate(cl);
        allowMethods(cl);
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        try {
            ClassFinder finder = new ClassFinder(CamelContext.class.getClassLoader());
            finder.findImplementations(Component.class).forEach(CamelFeature::allowAll);
            finder.findImplementations(Language.class).forEach(CamelFeature::allowAll);
            finder.findImplementations(DataFormat.class).forEach(CamelFeature::allowAll);
            finder.findImplementations(Consumer.class).forEach(CamelFeature::allowAll);
            finder.findImplementations(Producer.class).forEach(CamelFeature::allowAll);
            finder.findImplementations(Endpoint.class).forEach(CamelFeature::allowAll);
            finder.findImplementations(ProcessorDefinition.class).forEach(CamelFeature::allowAll);
            finder.findImplementations(ExchangeFormatter.class).forEach(CamelFeature::allowAll);
            finder.findImplementations(ExpressionDefinition.class).forEach(CamelFeature::allowAll);
            finder.findImplementations(TypeConverterLoader.class).forEach(CamelFeature::allowAll);
            finder.findAnnotatedClasses(Converter.class).forEach(CamelFeature::allowAll);
            finder.findAnnotatedMethods(Converter.class).forEach(RuntimeReflection::register);

            allowAll(GenericFileProcessStrategyFactory.class);
            allowAll(RouteDefinition.class);
            allowAll(FromDefinition.class);
            allowAll(ToDefinition.class);
            allowAll(SetBodyDefinition.class);
            allowAll(SetHeaderDefinition.class);
            allowAll(LanguageExpression.class);
            allowAll(ExpressionBuilder.class);
            allowAll(ExpressionClause.class);
            allowAll(BeanConverterLoader.class);
        } catch (Throwable t) {
            throw new RuntimeException("Unable to analyse classes", t);
        }
    }
}
