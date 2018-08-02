package org.apache.camel.graalvm;

import org.apache.camel.builder.RouteBuilder;
import org.graalvm.polyglot.Context;

public class ScriptedRouteBuilder extends RouteBuilder {
    private final String source;

    public ScriptedRouteBuilder(String source) {
        this.source = source;
    }

    public void configure() throws Exception {
        try(Context context = Context.create()) {
            // add this builder instance to javascript language
            // bindings
            //context.getBindings("js").putMember("builder", this);


            /*
            // move builder's methods to global scope
            context.eval(
                "js",
                "m = Object.keys(builder)\n" +
                    "m.forEach((element) => {\n" +
                    "    global[element] = builder[element]\n" +
                    "});"
            );
            try (InputStream is = new FileInputStream(source)) {
                context.eval(
                    Source.newBuilder("js", new InputStreamReader(is), "").build()
                );
            }
            */
        }
    }
}
