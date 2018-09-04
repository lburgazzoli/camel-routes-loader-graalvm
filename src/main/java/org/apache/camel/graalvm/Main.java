package org.apache.camel.graalvm;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        final SimpleRegistry registry = new SimpleRegistry();
        final CamelContext context = new FastCamelContext(registry);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                latch.countDown();
            }
        });

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                try(Context ctx = Context.create()) {
                    // add this builder instance to javascript language
                    // bindings
                    ctx.getBindings("js").putMember("from", new ProxyExecutable() {
                        @Override
                        public Object execute(Value... arguments) {
                            if (arguments.length != 1) {
                                throw new IllegalArgumentException("");
                            }
                            
                            final RouteDefinition def = from(arguments[0].asString());
                            final RouteDefinitionProxy answer = new RouteDefinitionProxy(def);
                            
                            return answer;
                        }
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

    private static class RouteDefinitionProxy implements ProxyObject {
        private final RouteDefinition definition;
        private final List<String> methods;

        public RouteDefinitionProxy(RouteDefinition definition) {
            this.definition = definition;
            this.methods = Arrays.asList("to" , "setBody", "setHeader");
        }

        @Override
        public Object getMember(String key) {
            LOGGER.info("key: {}", key);

            if ("to".equals(key)) {
                return new ProxyExecutable() {
                    @Override
                    public Object execute(Value... arguments) {
                        LOGGER.info("== to ==");
            
                        if (arguments.length != 1) {
                            throw new IllegalArgumentException("");
                        }

                        return new RouteDefinitionProxy(definition.to(arguments[0].asString()));
                    }
                };
            }

            if ("setBody".equals(key)) {
                return new ProxyExecutable() {
                    @Override
                    public Object execute(Value... arguments) {
                        LOGGER.info("== setBody ==");

                        if (arguments.length != 1) {
                            throw new IllegalArgumentException("");
                        }

                        definition.setBody().constant(arguments[0].asString());

                        return new RouteDefinitionProxy(definition);
                    }
                };
            }

            if ("setHeader".equals(key)) {
                return new ProxyExecutable() {
                    @Override
                    public Object execute(Value... arguments) {
                        LOGGER.info("== setHeader ==");

                        if (arguments.length != 2) {
                            throw new IllegalArgumentException("");
                        }

                        definition.setHeader(arguments[0].asString()).constant(arguments[1].asString());

                        return new RouteDefinitionProxy(definition);
                    }
                };
            }

            return null;
        }

        @Override
        public Object getMemberKeys() {
            return methods;
        }

        @Override
        public boolean hasMember(String key) {
            return methods.contains(key);
        }

        @Override
        public void putMember(String key, Value value) {
            throw new UnsupportedOperationException("Unsupported: " + key);
        }
    }
}
