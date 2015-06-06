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
package com.wrmsr.presto.jdbc.h2;

import com.facebook.presto.plugin.jdbc.BaseJdbcConfig;
import com.facebook.presto.plugin.jdbc.JdbcClient;
import com.facebook.presto.plugin.jdbc.JdbcConnectorId;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.wrmsr.presto.jdbc.ExtendedJdbcClient;
import org.h2.Driver;

import java.util.Map;

import static io.airlift.configuration.ConfigBinder.configBinder;
import static java.lang.String.format;

public class H2JdbcModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        // FIXME: spilits not remotely accessible, weave into extended bases
        configBinder(binder).bindConfig(BaseJdbcConfig.class);
        configBinder(binder).bindConfig(ExtendedJdbcClient.class);
    }

    @Provides
    public JdbcClient provideJdbcClient(JdbcConnectorId id, BaseJdbcConfig config)
    {
        return new ExtendedJdbcClient(id, config, "\"", new Driver());
    }

    public static Map<String, String> createProperties()
    {
        return ImmutableMap.<String, String>builder()
                .put("connection-url", format("jdbc:h2:mem:test%s;DB_CLOSE_DELAY=-1", System.nanoTime()))
                .build();
    }
}
