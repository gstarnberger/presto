/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wrmsr.presto.server;

import com.facebook.presto.server.PluginManager;
import com.facebook.presto.server.PluginManagerConfig;
import com.facebook.presto.spi.Plugin;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.wrmsr.presto.util.GuiceUtils;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.log.Logger;
import io.airlift.resolver.ArtifactResolver;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import static com.google.common.base.Preconditions.checkState;
import static io.airlift.configuration.ConfigBinder.configBinder;

public class PreloadedPlugins
{
    private static final Logger log = Logger.get(PreloadedPlugins.class);

    private PreloadedPlugins()
    {
    }

//    private static void patchJavaVersion()
//    {
//        try {
//            Field field = SystemUtils.class.getDeclaredField("JAVA_SPECIFICATION_VERSION_AS_ENUM");
//            field.setAccessible(true);
//            Field modifiersField = Field.class.getDeclaredField("modifiers");
//            modifiersField.setAccessible(true);
//            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
//            field.set(null, JavaVersion.JAVA_1_8);
//        }
//        catch (ReflectiveOperationException e) {
//            throw Throwables.propagate(e);
//        }
//    }

    public static Iterable<Module> processServerModules(Iterable<Module> modules)
    {
//        patchJavaVersion();

        Bootstrap app = new Bootstrap(ImmutableList.of(new Module()
        {
            @Override
            public void configure(Binder binder)
            {
                configBinder(binder).bindConfig(PluginManagerConfig.class);
            }
        }));
        PluginManagerConfig config;
        try {
            Injector injector = app.doNotInitializeLogging().initialize();
            config = injector.getInstance(PluginManagerConfig.class);
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }

        final PreloadedPluginSet preloadedPlugins = new PreloadedPluginSet();
        Module module = new GuiceUtils.CombineConfigurationAwareModule(modules);
        ArtifactResolver resolver = new ArtifactResolver(config.getMavenLocalRepository(), config.getMavenRemoteRepository());

        for (String preloadedPluginStr : config.getPreloadedPlugins()) {
            ClassLoader pluginClassLoader;
            try {
                pluginClassLoader = PluginManager.buildClassLoader(resolver, preloadedPluginStr);
            }
            catch (Exception e) {
                throw Throwables.propagate(e);
            }
            ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, pluginClassLoader);
            List<Plugin> plugins = ImmutableList.copyOf(serviceLoader);
            checkState(plugins.size() == 1);
            Plugin plugin = plugins.get(0);

            for (ModuleProcessor moduleProcessor : plugin.getServices(ModuleProcessor.class)) {
                log.info("Handling module processor: ", moduleProcessor);
                module = moduleProcessor.apply(module);
            }

            preloadedPlugins.add(plugin);
        }

        final Module finalModule = module;
        return ImmutableList.of(
                new GuiceUtils.OverrideConfigurationAwareModule(
                        ImmutableList.<Module>of(
                                new Module()
                                {
                                    @Override
                                    public void configure(Binder binder)
                                    {
                                        binder.bind(new TypeLiteral<Optional<PreloadedPluginSet>>() {}).toInstance(Optional.of(preloadedPlugins));
                                    }
                                }
                        ),
                        ImmutableSet.<Module>of(finalModule)
                ));
    }
}
