package org.apache.camel.graalvm;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.proxy.ProxyExecutable;

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
            try(Context ctx = Context.newBuilder("js").allowAllAccess(true).build()) {
                ctx.getBindings("js").putMember("from", (ProxyExecutable) arguments -> {
                    if (arguments.length != 1) {
                        throw new IllegalArgumentException("");
                    }

                    return from(arguments[0].asString());
                });

                ctx.eval(
                    Source.newBuilder("js", path.toFile()).build()
                );
            }
        }
    }
}
