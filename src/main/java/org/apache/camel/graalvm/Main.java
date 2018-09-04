package org.apache.camel.graalvm;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.RouteDefinition;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.proxy.Proxy;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

public class Main {
    public static void main(String[] args) throws Exception {
        final SimpleRegistry registry = new SimpleRegistry();
        final CamelContext context = new FastCamelContext(registry);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                try(Context ctx = Context.create()) {
                    // add this builder instance to javascript language
                    // bindings
                    ctx.getBindings("js").putMember("from", (ProxyExecutable) arguments -> {
                        if (arguments.length != 1) {
                            throw new IllegalArgumentException("");
                        }

                        return createRouteDefinitionProxy(
                            from(arguments[0].asString())
                        );
                    });

                    ctx.eval(
                        Source.newBuilder("js", new File(args[0])).build()
                    );
                }
            }
        });

        try {
            context.start();
            latch.await();
        } finally {
            context.stop();
        }
    }

    private static Proxy createRouteDefinitionProxy(RouteDefinition definition) {
        Map<String, Object> methods = new HashMap<>();
        methods.put("to", (ProxyExecutable) arguments -> {
            if (arguments.length != 1) {
                throw new IllegalArgumentException("");
            }

            return createRouteDefinitionProxy(definition.to(arguments[0].asString()));
        });
        methods.put("setBody", (ProxyExecutable) arguments -> {
            if (arguments.length != 1) {
                throw new IllegalArgumentException("");
            }

            definition.setBody().constant(arguments[0].asString());

            return createRouteDefinitionProxy(definition);
        });
        methods.put("setHeader", (ProxyExecutable) arguments -> {
            if (arguments.length != 2) {
                throw new IllegalArgumentException("");
            }

            definition.setHeader(arguments[0].asString()).constant(arguments[1].asString());

            return createRouteDefinitionProxy(definition);
        });

        return ProxyObject.fromMap(methods);
    }
}
