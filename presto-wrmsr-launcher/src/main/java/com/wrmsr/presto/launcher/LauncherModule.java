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
package com.wrmsr.presto.launcher;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.wrmsr.presto.launcher.config.ConfigContainer;
import io.airlift.airline.Cli;

import java.util.List;
import java.util.function.Function;

public abstract class LauncherModule
{
    public void configureCli(Cli.CliBuilder<Runnable> builder)
    {
    }

    public ConfigContainer postprocessConfig(ConfigContainer config)
    {
        return config;
    }

    public ConfigContainer rewriteConfig(ConfigContainer config, Function<ConfigContainer, ConfigContainer> postprocess)
    {
        return config;
    }

    public void configureLauncher(ConfigContainer config, Binder binder)
    {
    }

    public void configureServerLogging(ConfigContainer config)
    {
    }

    public static class Composite
            extends LauncherModule
    {
        private final List<LauncherModule> children;

        public Composite(Iterable<LauncherModule> children)
        {
            this.children = ImmutableList.copyOf(children);
        }

        public Composite(LauncherModule first, LauncherModule... rest)
        {
            this(ImmutableList.<LauncherModule>builder().add(first).add(rest).build());
        }

        @Override
        public void configureCli(Cli.CliBuilder<Runnable> builder)
        {
            configureCliParent(builder);
            for (LauncherModule child : children) {
                child.configureCli(builder);
            }
        }

        @Override
        public final ConfigContainer postprocessConfig(ConfigContainer config)
        {
            config = postprocessConfigParent(config);
            for (LauncherModule child : children) {
                config = child.postprocessConfig(config);
            }
            return config;
        }

        @Override
        public final ConfigContainer rewriteConfig(ConfigContainer config, Function<ConfigContainer, ConfigContainer> postprocess)
        {
            config = rewriteConfigParent(config, postprocess);
            for (LauncherModule child : children) {
                config = child.rewriteConfig(config, postprocess);
            }
            return config;
        }

        @Override
        public final void configureLauncher(ConfigContainer config, Binder binder)
        {
            configureLauncherParent(config, binder);
            for (LauncherModule child : children) {
                child.configureLauncher(config, binder);
            }
        }

        @Override
        public void configureServerLogging(ConfigContainer config)
        {
            configureServerLoggingParent(config);
            for (LauncherModule child : children) {
                child.configureServerLogging(config);
            }
        }

        protected void configureCliParent(Cli.CliBuilder<Runnable> builder)
        {
        }

        protected ConfigContainer postprocessConfigParent(ConfigContainer config)
        {
            return config;
        }

        protected ConfigContainer rewriteConfigParent(ConfigContainer config, Function<ConfigContainer, ConfigContainer> postprocess)
        {
            return config;
        }

        protected void configureLauncherParent(ConfigContainer config, Binder binder)
        {
        }

        protected void configureServerLoggingParent(ConfigContainer config)
        {
        }
    }
}
