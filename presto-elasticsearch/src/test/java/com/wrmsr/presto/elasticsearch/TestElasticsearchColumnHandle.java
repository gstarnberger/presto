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
package com.wrmsr.presto.elasticsearch;

import io.airlift.testing.EquivalenceTester;
import org.testng.annotations.Test;

import static com.wrmsr.presto.elasticsearch.MetadataUtil.COLUMN_CODEC;
import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.spi.type.VarcharType.VARCHAR;
import static org.testng.Assert.assertEquals;

public class TestElasticsearchColumnHandle
{
    private final ElasticsearchColumnHandle columnHandle = new ElasticsearchColumnHandle("connectorId", "columnName", VARCHAR, 0);

    @Test
    public void testJsonRoundTrip()
    {
        String json = COLUMN_CODEC.toJson(columnHandle);
        ElasticsearchColumnHandle copy = COLUMN_CODEC.fromJson(json);
        assertEquals(copy, columnHandle);
    }

    @Test
    public void testEquivalence()
    {
        EquivalenceTester.equivalenceTester()
                .addEquivalentGroup(
                        new ElasticsearchColumnHandle("connectorId", "columnName", VARCHAR, 0),
                        new ElasticsearchColumnHandle("connectorId", "columnName", VARCHAR, 0),
                        new ElasticsearchColumnHandle("connectorId", "columnName", BIGINT, 0),
                        new ElasticsearchColumnHandle("connectorId", "columnName", VARCHAR, 1))
                .addEquivalentGroup(
                        new ElasticsearchColumnHandle("connectorIdX", "columnName", VARCHAR, 0),
                        new ElasticsearchColumnHandle("connectorIdX", "columnName", VARCHAR, 0),
                        new ElasticsearchColumnHandle("connectorIdX", "columnName", BIGINT, 0),
                        new ElasticsearchColumnHandle("connectorIdX", "columnName", VARCHAR, 1))
                .addEquivalentGroup(
                        new ElasticsearchColumnHandle("connectorId", "columnNameX", VARCHAR, 0),
                        new ElasticsearchColumnHandle("connectorId", "columnNameX", VARCHAR, 0),
                        new ElasticsearchColumnHandle("connectorId", "columnNameX", BIGINT, 0),
                        new ElasticsearchColumnHandle("connectorId", "columnNameX", VARCHAR, 1))
                .check();
    }
}
