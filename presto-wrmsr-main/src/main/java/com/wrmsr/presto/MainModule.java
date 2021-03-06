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
package com.wrmsr.presto;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.wrmsr.presto.config.ConfigContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class MainModule
{
    public Module processServerModule(ConfigContainer config, Module module)
    {
        return module;
    }

    public Set<Key> getInjectorForwardings(ConfigContainer config)
    {
        return ImmutableSet.of();
    }

    public void configurePlugin(ConfigContainer config, Binder binder)
    {
    }

    public static class Composite
            extends MainModule
    {
        private final List<MainModule> children;

        public Composite(Iterable<MainModule> children)
        {
            this.children = ImmutableList.copyOf(children);
        }

        public Composite(MainModule first, MainModule... rest)
        {
            this(ImmutableList.<MainModule>builder().add(first).add(rest).build());
        }

        @Override
        public final Module processServerModule(ConfigContainer config, Module module)
        {
            module = processServerModuleParent(config, module);
            for (MainModule child : children) {
                module = child.processServerModule(config, module);
            }
            return module;
        }

        @Override
        public final Set<Key> getInjectorForwardings(ConfigContainer config)
        {
            Set<Key> tmp = new HashSet<>();
            tmp.addAll(getInjectorForwardingsParent(config));
            for (MainModule child : children) {
                tmp.addAll(child.getInjectorForwardings(config));
            }
            return ImmutableSet.copyOf(tmp);
        }

        @Override
        public final void configurePlugin(ConfigContainer config, Binder binder)
        {
            configurePluginParent(config, binder);
            for (MainModule child : children) {
                child.configurePlugin(config, binder);
            }
        }

        protected Module processServerModuleParent(ConfigContainer config, Module module)
        {
            return module;
        }

        protected Set<Key> getInjectorForwardingsParent(ConfigContainer config)
        {
            return ImmutableSet.of();
        }

        protected void configurePluginParent(ConfigContainer config, Binder binder)
        {
        }
    }
}
