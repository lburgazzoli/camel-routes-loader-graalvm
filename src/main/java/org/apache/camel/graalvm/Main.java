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

    private static Proxy createRouteDefinitionProxy(RouteDefinition def) {
        Map<String, Object> methods = new HashMap<>();

        methods.put("to", (ProxyExecutable) args -> {
            if (args.length != 1) {
                throw new IllegalArgumentException("");
            }

            // wrap the definition with a new
            // proxy
            return createRouteDefinitionProxy(
                def.to(args[0].asString())
            );
        });

        methods.put("setBody", (ProxyExecutable) args -> {
            if (args.length != 1) {
                throw new IllegalArgumentException("");
            }

            // assuming we only use strings in js
            def.setBody().constant(args[0].asString());

            // wrap the definition with a new
            // proxy
            return createRouteDefinitionProxy(def);
        });

        methods.put("setHeader", (ProxyExecutable) args -> {
            if (args.length != 2) {
                throw new IllegalArgumentException("");
            }

            // assuming we only use strings in js
            final String key = args[0].asString();
            final String val = args[0].asString();

            def.setHeader(key).constant(val);

            // wrap the definition with a new
            // proxy
            return createRouteDefinitionProxy(def);
        });

        return ProxyObject.fromMap(methods);
    }
}
