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
package com.wrmsr.presto.util.jdbc;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.wrmsr.presto.util.collect.CaseInsensitiveMap;
import com.wrmsr.presto.util.ColumnDomain;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;

public class Queries
{
    private Queries()
    {
    }

    public static Map<String, Object> readRow(ResultSet rs, ResultSetMetaData md)
            throws SQLException
    {
        Map<String, Object> row = new CaseInsensitiveMap<>();
        int columns = md.getColumnCount();
        for (int i = 1; i <= columns; ++i) {
            row.put(md.getColumnName(i), rs.getObject(i));
        }
        return row;
    }

    public static Map<String, Object> readRow(ResultSet rs)
            throws SQLException
    {
        return readRow(rs, rs.getMetaData());
    }

    public static List<Map<String, Object>> readResultSet(ResultSet rs)
            throws SQLException
    {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String, Object>> list = new ArrayList<>(64);
        while (rs.next()) {
            list.add(readRow(rs, md));
        }
        return list;
    }

    public static List<Map<String, Object>> select(Connection conn, String query,
            Object... params)
            throws SQLException
    {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; ++i) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet result = stmt.executeQuery()) {
                return readResultSet(result);
            }
        }
    }

    public static Map<String, Object> one(Connection conn, String query, Object... params)
            throws SQLException
    {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; ++i) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet result = stmt.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                Map<String, Object> row = readRow(result);
                // if (result.next())
                //    throw
                return row;
            }
        }
    }

    public static Object scalar(Connection conn, String query, Object... params)
            throws SQLException
    {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; ++i) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet result = stmt.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return result.getObject(1);
            }
        }
    }

    private static final List<String> MIN_AND_MAX = ImmutableList.of("MIN", "MAX");

    public static List<String> getClusteredColumns(Connection connection, String schemaName, String tableName)
            throws SQLException, IOException
    {
        String clusteredIndexName = null;
        Map<Integer, String> clusteredColumnsByOrdinal = new HashMap<>();
        DatabaseMetaData metadata = connection.getMetaData();

        // FIXME postgres catalog support
        try (ResultSet resultSet = metadata.getIndexInfo(schemaName, schemaName, tableName, false, false)) {
            while (resultSet.next()) {
                if (resultSet.getShort("TYPE") != DatabaseMetaData.tableIndexClustered) {
                    continue;
                }

                String indexName = checkNotNull(resultSet.getString("INDEX_NAME"));
                if (clusteredColumnsByOrdinal.isEmpty()) {
                    checkState(clusteredIndexName == null);
                    clusteredIndexName = indexName;
                }
                else {
                    checkState(indexName.equals(clusteredIndexName));
                }

                int ordinalPosition = resultSet.getInt("ORDINAL_POSITION");
                String columnName = checkNotNull(resultSet.getString("COLUMN_NAME"));
                // boolean isDescending = resultSet.getBoolean("ASC_OR_DESC"); // FIXME
                checkState(!clusteredColumnsByOrdinal.containsKey(ordinalPosition));
                clusteredColumnsByOrdinal.put(ordinalPosition, columnName);
            }
        }

        if (clusteredIndexName == null) {
            // FIXME postgres catalog support
            try (ResultSet resultSet = metadata.getPrimaryKeys(schemaName, schemaName, tableName)) {
                while (resultSet.next()) {
                    int ordinalPosition = resultSet.getInt("KEY_SEQ");
                    String columnName = checkNotNull(resultSet.getString("COLUMN_NAME"));
                    checkState(!clusteredColumnsByOrdinal.containsKey(ordinalPosition));
                    clusteredColumnsByOrdinal.put(ordinalPosition, columnName);
                }
            }
        }

        List<String> clusteredColumns = IntStream.range(1, clusteredColumnsByOrdinal.size() + 1).boxed()
                .map(i -> clusteredColumnsByOrdinal.get(i))
                .collect(Collectors.toList());
        checkState(Sets.newHashSet(clusteredColumns).size() == clusteredColumns.size());
        return clusteredColumns;
    }

    public static Map<String, ColumnDomain> getColumnDomains(
            Connection connection,
            String catalogName,
            String schemaName,
            String tableName,
            List<String> columnNames,
            Function<String, String> quote
    )
            throws SQLException, IOException
    {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        Joiner.on(", ").appendTo(sql, columnNames.stream().flatMap(c -> MIN_AND_MAX.stream().map(f -> String.format("%s(%s)", f, quote.apply(c)))).collect(Collectors.toList()));

        sql.append(" FROM ");
        if (!isNullOrEmpty(catalogName)) {
            sql.append(quote.apply(catalogName)).append('.');
        }
        if (!isNullOrEmpty(schemaName)) {
            sql.append(quote.apply(schemaName)).append('.');
        }
        sql.append(quote.apply(tableName));

        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql.toString())) {
            checkState(result.next());
            checkState(result.getMetaData().getColumnCount() == columnNames.size() * 2);
            Map<String, ColumnDomain> ret = new HashMap<>();
            for (int i = 0; i < columnNames.size(); ++i) {
                ret.put(columnNames.get(i),
                        new ColumnDomain(
                                (Comparable<?>) result.getObject((i * 2) + 1),
                                (Comparable<?>) result.getObject((i * 2) + 2)));
            }
            return ret;
        }
    }
}
