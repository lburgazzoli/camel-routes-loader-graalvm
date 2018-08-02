package org.apache.camel.graalvm;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.SimpleRegistry;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

public class Main {
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
                try(Context ctx = Context.create()) {// add this builder instance to javascript language
                    // bindings
                    ctx.getBindings("js").putMember("builder", this);
                    
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
}
