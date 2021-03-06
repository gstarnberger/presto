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

import com.facebook.presto.plugin.jdbc.BaseJdbcConfig;
import com.facebook.presto.plugin.jdbc.JdbcConnectorId;
import com.facebook.presto.plugin.jdbc.JdbcOutputTableHandle;
import com.google.common.base.Throwables;
import com.wrmsr.presto.connector.jdbc.ExtendedJdbcClient;
import com.wrmsr.presto.connector.jdbc.ExtendedJdbcConfig;
import io.airlift.slice.Slice;
import org.postgresql.Driver;

import javax.inject.Inject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import static com.wrmsr.presto.util.Exceptions.runtimeThrowing;

public class ExtendedPostgreSqlClient
        extends ExtendedJdbcClient
{
    @Inject
    public ExtendedPostgreSqlClient(JdbcConnectorId connectorId, BaseJdbcConfig config, ExtendedJdbcConfig extendedConfig)
    {
        super(connectorId, config, extendedConfig, "\"", createDriver(extendedConfig, runtimeThrowing(Driver::new)));
    }

    @Override
    public void commitCreateTable(JdbcOutputTableHandle handle, Collection<Slice> fragments)
    {
        // PostgreSQL does not allow qualifying the target of a rename
        StringBuilder sql = new StringBuilder()
                .append("ALTER TABLE ")
                .append(quoted(handle.getCatalogName(), handle.getSchemaName(), handle.getTemporaryTableName()))
                .append(" RENAME TO ")
                .append(quoted(handle.getTableName()));

        try (Connection connection = getConnection(handle)) {
            execute(connection, sql.toString());
        }
        catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }
}
