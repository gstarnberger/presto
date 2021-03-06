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
package com.wrmsr.presto.connector.partitioner;

import com.facebook.presto.spi.NodeManager;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

import javax.annotation.Nullable;

import static io.airlift.configuration.ConfigBinder.configBinder;

public class PartitionerModule
        implements Module
{
    @Nullable
    private final NodeManager nodeManager;

    public PartitionerModule(@Nullable NodeManager nodeManager)
    {
        this.nodeManager = nodeManager;
    }

    @Override
    public void configure(Binder binder)
    {
        //binder.bind(NodeManager.class)
        /*
        binder.bind(JdbcMetadata.class).in(Scopes.SINGLETON);
        binder.bind(JdbcSplitManager.class).in(Scopes.SINGLETON);
        binder.bind(JdbcRecordSetProvider.class).in(Scopes.SINGLETON);
        binder.bind(JdbcHandleResolver.class).in(Scopes.SINGLETON);
        binder.bind(JdbcRecordSinkProvider.class).in(Scopes.SINGLETON);
        binder.bind(JdbcConnector.class).in(Scopes.SINGLETON);
        */
        binder.bind(PartitionerConnector.class).in(Scopes.SINGLETON);
        configBinder(binder).bindConfig(PartitionerConfig.class);
    }
}
