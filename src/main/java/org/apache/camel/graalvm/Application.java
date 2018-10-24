package org.apache.camel.graalvm;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.camel.model.RouteDefinition;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

public class Application {
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.addRouteBuilder(new Routes(Paths.get(args[0])));
        main.run();
    }

    // ***********************
    //
    // Routes
    //
    // ***********************

    private static class Routes extends RouteBuilder {
        private final Path path;

        public Routes(Path path) {
            this.path = path;
        }

        @Override
        public void configure() throws Exception {
            try(Context ctx = Context.create()) {
                ctx.getBindings("js").putMember("from", (ProxyExecutable) arguments -> {
                    if (arguments.length != 1) {
                        throw new IllegalArgumentException("");
                    }

                    return new RouteDefinitionProxy(
                        getContext(),
                        from(arguments[0].asString())
                    );
                });

                ctx.eval(
                    Source.newBuilder("js", path.toFile()).build()
                );
            }
        }
    }

    // ***********************
    //
    // Definition Proxy
    //
    // ***********************

    private static class RouteDefinitionProxy implements ProxyObject {
        private final CamelContext context;
        private final RouteDefinition parent;
        private final List<String> members;

        public RouteDefinitionProxy(CamelContext context, RouteDefinition parent) {
            this.context = context;
            this.parent = parent;
            this.members = Arrays.asList("to", "setBody", "setHeader");
        }

        @Override
        public void putMember(String key, Value value) {
            throw new UnsupportedOperationException(key);
        }

        @Override
        public Object getMemberKeys() {
            return this.members;
        }

        @Override
        public boolean hasMember(String key) {
            return this.members.contains(key);
        }

        @Override
        public Object getMember(String key) {
            switch (key) {
            case "to":
                return (ProxyExecutable) args -> {
                    if (args.length != 1) {
                        throw new IllegalArgumentException("");
                    }

                    return new RouteDefinitionProxy(context, parent.to(args[0].asString()));
                };
            case "setBody":
                return (ProxyExecutable) args -> {
                    if (args.length != 1) {
                        throw new IllegalArgumentException("");
                    }

                    // assuming we only use strings in js
                    parent.setBody().constant(args[0].asString());

                    return new RouteDefinitionProxy(context, parent);
                };
            case "setHeader":
                return (ProxyExecutable) args -> {
                    if (args.length != 2) {
                        throw new IllegalArgumentException("");
                    }

                    // assuming we only use strings in js
                    final String headerKey = args[0].asString();
                    final String headerVal = args[1].asString();

                    parent.setHeader(headerKey).constant(headerVal);

                    return new RouteDefinitionProxy(context, parent);
                };
            }

            throw new UnsupportedOperationException(key);
        }
    }
}
