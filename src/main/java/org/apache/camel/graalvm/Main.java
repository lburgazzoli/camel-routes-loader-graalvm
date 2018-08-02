package org.apache.camel.graalvm;


import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.SimpleRegistry;
import org.graalvm.polyglot.Context;

public class Main {
    public static void main(String[] args) throws Exception {
        final SimpleRegistry registry = new SimpleRegistry();
        final CamelContext context = new FastCamelContext(registry);

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                Context.create();
            }
        });
    }
}
