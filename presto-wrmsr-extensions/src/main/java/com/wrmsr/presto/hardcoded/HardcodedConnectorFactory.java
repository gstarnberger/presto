package com.wrmsr.presto.hardcoded;

import com.facebook.presto.spi.Connector;
import com.facebook.presto.spi.ConnectorFactory;
import com.facebook.presto.spi.SchemaTableName;
import com.facebook.presto.spi.classloader.ThreadContextClassLoader;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.wrmsr.presto.util.Configs;
import com.wrmsr.presto.util.ImmutableCollectors;
import io.airlift.bootstrap.Bootstrap;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Preconditions.checkArgument;

public class HardcodedConnectorFactory
    implements ConnectorFactory
{
    private final Map<String, String> optionalConfig;
    private final Module module;
    private final ClassLoader classLoader;

    public HardcodedConnectorFactory(Map<String, String> optionalConfig, Module module, ClassLoader classLoader)
    {
        this.optionalConfig = ImmutableMap.copyOf(checkNotNull(optionalConfig, "optionalConfig is null"));
        this.module = checkNotNull(module, "module is null");
        this.classLoader = checkNotNull(classLoader, "classLoader is null");
    }

    @Override
    public String getName()
    {
        return "hardcoded";
    }

    private SchemaTableName toName(String name)
    {
        List<String> parts = Splitter.on('.').splitToList(checkNotNull(name));
        checkArgument(parts.size() == 2);
        return new SchemaTableName(parts.get(0), parts.get(1));
    }
    @Override
    public Connector create(String connectorId, Map<String, String> requiredConfiguration)
    {
        checkNotNull(requiredConfiguration, "requiredConfiguration is null");
        requiredConfiguration = Maps.newHashMap(requiredConfiguration);

        Map<SchemaTableName, String> views = Configs.stripSubconfig(requiredConfiguration, "views").entrySet().stream()
                .collect(ImmutableCollectors.toImmutableMap(e -> toName(e.getKey()), e -> e.getValue()));
        HardcodedContents contents =  new HardcodedContents(ImmutableMap.copyOf(views));

        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(classLoader)) {
            Bootstrap app = new Bootstrap(module, new Module()
            {
                @Override
                public void configure(Binder binder)
                {
                    binder.bind(HardcodedConnectorId.class).toInstance(new HardcodedConnectorId(connectorId));
                    binder.bind(HardcodedContents.class).toInstance(contents);
                }
            });

            Injector injector = app
                    .strictConfig()
                    .doNotInitializeLogging()
                    .setRequiredConfigurationProperties(requiredConfiguration)
                    .setOptionalConfigurationProperties(optionalConfig)
                    .initialize();

            return injector.getInstance(HardcodedConnector.class);
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
