package org.apache.camel.graalvm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.camel.builder.BuilderSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.camel.model.RouteDefinition;
import org.graalvm.polyglot.Context;

public class Application {
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.addRouteBuilder(new Routes(Paths.get(args[0])));
        main.run();
    }

    private static class Routes extends RouteBuilder {
        private final Path path;
        private final Context context;

        public Routes(Path path) {
            this.path = path;
            this.context = Context.newBuilder("js").allowAllAccess(true).build();
        }

        @Override
        public void configure() throws Exception {
            final DSL dsl = new DSL(this);
            final byte[] content = Files.readAllBytes(path);

            this.context.getBindings("js").putMember("__dsl", dsl);
            this.context.eval("js", "with (__dsl) { " + new String(content) + "}");
        }
    }


    public static class DSL extends BuilderSupport {
        private RouteBuilder builder;

        public DSL(RouteBuilder builder) {
            this.builder = builder;
        }

        public RouteDefinition from(String uri) {
            return builder.from(uri);
        }
    }
}
