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

import com.facebook.presto.Session;
import com.facebook.presto.metadata.SessionPropertyManager;
import com.facebook.presto.testing.QueryRunner;
import com.facebook.presto.tests.DistributedQueryRunner;
import com.facebook.presto.tpch.TpchPlugin;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.presto.MainPlugin;
import io.airlift.testing.postgresql.TestingPostgreSqlServer;
import io.airlift.tpch.TpchTable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static com.facebook.presto.spi.type.TimeZoneKey.UTC_KEY;
import static com.facebook.presto.tests.QueryAssertions.copyTpchTables;
import static com.facebook.presto.tpch.TpchMetadata.TINY_SCHEMA_NAME;
import static io.airlift.testing.Closeables.closeAllSuppress;
import static java.util.Locale.ENGLISH;

public final class PostgreSqlQueryRunner
{
    private PostgreSqlQueryRunner()
    {
    }

    private static final String TPCH_SCHEMA = "tpch";

    public static QueryRunner createPostgreSqlQueryRunner(TestingPostgreSqlServer server, TpchTable<?>... tables)
            throws Exception
    {
        return createPostgreSqlQueryRunner(server, ImmutableList.copyOf(tables));
    }

    public static QueryRunner createPostgreSqlQueryRunner(TestingPostgreSqlServer server, Iterable<TpchTable<?>> tables)
            throws Exception
    {
        DistributedQueryRunner queryRunner = null;
        try {
            queryRunner = new DistributedQueryRunner(createSession(), 3);

            queryRunner.installPlugin(new TpchPlugin());
            queryRunner.createCatalog("tpch", "tpch");

            Map<String, String> properties = ImmutableMap.<String, String>builder()
                    .put("connection-url", server.getJdbcUrl())
                    .put("allow-drop-table", "true")
                    .build();

            createSchema(server.getJdbcUrl(), "tpch");

            queryRunner.installPlugin(new MainPlugin());
            queryRunner.createCatalog("postgresql", "extended-postgresql", properties);

            copyTpchTables(queryRunner, "tpch", TINY_SCHEMA_NAME, createSession(), tables);

            return queryRunner;
        }
        catch (Throwable e) {
            closeAllSuppress(e, queryRunner, server);
            throw e;
        }
    }

    private static void createSchema(String url, String schema)
            throws SQLException
    {
        try (Connection connection = DriverManager.getConnection(url);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schema);
        }
    }

    public static Session createSession()
    {
        return Session.builder(new SessionPropertyManager())
                .setSource("test")
                .setCatalog("postgresql")
                .setSchema(TPCH_SCHEMA)
                .setTimeZoneKey(UTC_KEY)
                .setLocale(ENGLISH)
                .build();
    }
}
