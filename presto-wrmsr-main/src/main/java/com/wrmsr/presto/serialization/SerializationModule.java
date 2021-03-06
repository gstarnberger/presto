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
package com.wrmsr.presto.serialization;

import com.facebook.presto.metadata.SqlFunction;
import com.facebook.presto.spi.type.TypeManager;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.wrmsr.presto.MainModule;
import com.wrmsr.presto.config.ConfigContainer;
import com.wrmsr.presto.serialization.boxing.BoxingModule;

import java.util.Set;

public class SerializationModule
    extends MainModule.Composite
{
    public SerializationModule()
    {
        super(
                new BoxingModule());
    }

    @Override
    protected Set<Key> getInjectorForwardingsParent(ConfigContainer config)
    {
        return ImmutableSet.of(
                Key.get(TypeManager.class));
    }

    @Override
    protected void configurePluginParent(ConfigContainer config, Binder binder)
    {
        Multibinder<SqlFunction> functionBinder = Multibinder.newSetBinder(binder, SqlFunction.class);

        binder.bind(SerializeFunction.class).asEagerSingleton();
        functionBinder.addBinding().to(SerializeFunction.class);
    }
}
