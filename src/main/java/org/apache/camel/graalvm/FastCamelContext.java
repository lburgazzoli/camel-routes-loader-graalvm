package org.apache.camel.graalvm;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.TypeConverter;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultInjector;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.impl.SimpleUuidGenerator;
import org.apache.camel.spi.Injector;
import org.apache.camel.spi.ManagementStrategy;
import org.apache.camel.spi.ShutdownStrategy;
import org.apache.camel.spi.UuidGenerator;
import org.apache.camel.util.EventHelper;

public class FastCamelContext extends DefaultCamelContext {

    public FastCamelContext(SimpleRegistry registry) {
        super(registry);
    }

    @Override
    public void addRoutes(final RoutesBuilder builder) throws Exception {
        builder.addRoutesToCamelContext(this);
    }

    @Override
    protected synchronized void doStart() throws Exception {
        try {
            doStartCamel();
        } catch (Exception e) {
            // fire event that we failed to start
            EventHelper.notifyCamelContextStartupFailed(this, e);
            // rethrow cause
            throw e;
        }
    }

    @Override
    protected Injector createInjector() {
        return new DefaultInjector(this);
    }

    @Override
    protected UuidGenerator createDefaultUuidGenerator() {
        return new SimpleUuidGenerator();
    }

    @Override
    protected ManagementStrategy createManagementStrategy() {
        return new NoManagementStrategy();
    }

    @Override
    protected TypeConverter createTypeConverter() {
        FastTypeConverterRegistry answer
                = new FastTypeConverterRegistry(this, getPackageScanClassResolver(), getInjector(), getDefaultFactoryFinder());
        setTypeConverterRegistry(answer);
        return answer;
    }

    @Override
    public Boolean isTypeConverterStatisticsEnabled() {
        return null;
    }

    @Override
    protected ShutdownStrategy createShutdownStrategy() {
        return new NoShutdownStrategy();
    }

}
