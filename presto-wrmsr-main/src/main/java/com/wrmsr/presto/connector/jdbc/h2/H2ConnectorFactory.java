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
package com.wrmsr.presto.connector.jdbc.h2;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.presto.MainOptionalConfig;
import com.wrmsr.presto.connector.Connectors;
import com.wrmsr.presto.connector.jdbc.ExtendedJdbcConnectorFactory;

import javax.inject.Inject;

public class H2ConnectorFactory
        extends ExtendedJdbcConnectorFactory
{
    public H2ConnectorFactory()
    {
        this(MainOptionalConfig.EMPTY);
    }

    @Inject
    public H2ConnectorFactory(MainOptionalConfig optionalConfig)
    {
        super("h2", new H2ClientModule(), optionalConfig.getValue(), ImmutableMap.of(), Connectors.getClassLoader());
    }
}
