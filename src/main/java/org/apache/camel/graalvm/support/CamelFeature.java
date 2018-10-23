package org.apache.camel.graalvm.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Producer;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.Language;
import org.apache.xbean.finder.ClassFinder;
import org.graalvm.nativeimage.Feature;
import org.graalvm.nativeimage.RuntimeReflection;

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

            allowInstantiate(org.apache.camel.component.file.strategy.GenericFileProcessStrategyFactory.class);
            allowMethods(org.apache.camel.component.file.strategy.GenericFileProcessStrategyFactory.class);
            allowMethods(org.apache.camel.model.RouteDefinition.class);
            allowMethods(org.apache.camel.model.FromDefinition.class);
            allowMethods(org.apache.camel.model.ToDefinition.class);
        } catch (Throwable t) {
            throw new RuntimeException("Unable to analyse classes", t);
        }
    }
}
