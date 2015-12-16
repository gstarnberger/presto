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
package com.wrmsr.presto.connectorSupport;

import com.facebook.presto.spi.ColumnHandle;
import com.facebook.presto.spi.Connector;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.ConnectorTableHandle;
import com.facebook.presto.spi.SchemaTableName;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.tpch.TpchColumnHandle;
import com.facebook.presto.tpch.TpchTableHandle;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.presto.spi.ConnectorSupport;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public class TpchConnectorSupport
        extends ConnectorSupport<Connector>
{
    private final String defaultSchema;

    private static final Set<String> schemas = ImmutableSet.<String>builder()
            .add("tiny")
            .build();

    private static final Map<String, List<String>> tablePrimaryKeys = ImmutableMap.<String, List<String>>builder()
            .put("customer", ImmutableList.of("custkey"))
            .put("supplier", ImmutableList.of("suppkey"))
            .put("part", ImmutableList.of("partkey"))
            .put("nation", ImmutableList.of("nationkey"))
            .put("region", ImmutableList.of("regionkey"))
            .put("partsupp", ImmutableList.of("partkey", "suppkey"))
            .put("orders", ImmutableList.of("orderkey"))
            .build();

    public TpchConnectorSupport(ConnectorSession session, Connector connector, String defaultSchema)
    {
        super(session, connector);
        checkArgument(schemas.contains(defaultSchema));
        this.defaultSchema = defaultSchema;
    }

    @Override
    public SchemaTableName getSchemaTableName(ConnectorTableHandle handle)
    {
        checkArgument(handle instanceof TpchTableHandle);
        return new SchemaTableName(defaultSchema, ((TpchTableHandle) handle).getTableName());
    }

    @Override
    public List<String> getPrimaryKey(SchemaTableName schemaTableName)
    {
        checkArgument(schemas.contains(schemaTableName.getSchemaName()));
        checkArgument(tablePrimaryKeys.containsKey(schemaTableName.getTableName()));
        return tablePrimaryKeys.get(schemaTableName.getTableName());
    }

    @Override
    public String getColumnName(ColumnHandle columnHandle)
    {
        return ((TpchColumnHandle) columnHandle).getColumnName();
    }

    @Override
    public Type getColumnType(ColumnHandle columnHandle)
    {
        return ((TpchColumnHandle) columnHandle).getType();
    }
}
