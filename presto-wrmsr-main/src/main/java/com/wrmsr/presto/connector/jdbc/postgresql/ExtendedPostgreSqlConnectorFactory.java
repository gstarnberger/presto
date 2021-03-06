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
package com.wrmsr.presto.connector.jdbc.postgresql;

import com.google.common.collect.ImmutableMap;
import com.wrmsr.presto.MainOptionalConfig;
import com.wrmsr.presto.connector.Connectors;
import com.wrmsr.presto.connector.jdbc.ExtendedJdbcConnectorFactory;
import com.wrmsr.presto.connector.jdbc.mysql.ExtendedMySqlClientModule;

import javax.inject.Inject;

public class ExtendedPostgreSqlConnectorFactory
        extends ExtendedJdbcConnectorFactory
{
    public ExtendedPostgreSqlConnectorFactory()
    {
        this(MainOptionalConfig.EMPTY);
    }

    @Inject
    public ExtendedPostgreSqlConnectorFactory(MainOptionalConfig optionalConfig)
    {
        super("extended-postgresql", new ExtendedMySqlClientModule(), optionalConfig.getValue(), ImmutableMap.of(), Connectors.getClassLoader());
    }
}
