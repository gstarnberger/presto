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

import com.facebook.presto.spi.SystemTable;
import com.facebook.presto.spi.connector.Connector;
import com.facebook.presto.spi.connector.ConnectorMetadata;
import com.facebook.presto.spi.connector.ConnectorPageSinkProvider;
import com.facebook.presto.spi.connector.ConnectorPageSourceProvider;
import com.facebook.presto.spi.connector.ConnectorRecordSetProvider;
import com.facebook.presto.spi.connector.ConnectorRecordSinkProvider;
import com.facebook.presto.spi.connector.ConnectorSplitManager;
import com.facebook.presto.spi.connector.ConnectorTransactionHandle;
import com.facebook.presto.spi.transaction.IsolationLevel;
import com.google.inject.Inject;

import java.util.Set;

public class PartitionerConnector
        implements Connector
{
    private final PartitionerConnectorId connectorId;
    private final PartitionerTarget target;
    private final Partitioner partitioner;

    @Inject
    public PartitionerConnector(PartitionerConnectorId connectorId, PartitionerTarget target, Partitioner partitioner)
    {
        this.connectorId = connectorId;
        this.target = target;
        this.partitioner = partitioner;
    }

    @Override
    public ConnectorTransactionHandle beginTransaction(IsolationLevel isolationLevel, boolean readOnly)
    {
        return target.getTarget().beginTransaction(isolationLevel, readOnly);
    }

    @Override
    public ConnectorMetadata getMetadata(ConnectorTransactionHandle transactionHandle)
    {
        return new PartitionerMetadata(target.getTarget().getMetadata(transactionHandle));
    }

    @Override
    public ConnectorSplitManager getSplitManager()
    {
        return new PartitionerSplitManager(connectorId.toString(), target.getTarget(), target.getTarget().getSplitManager(), partitioner);
    }

    @Override
    public ConnectorPageSourceProvider getPageSourceProvider()
    {
        return target.getTarget().getPageSourceProvider();
    }

    @Override
    public ConnectorRecordSetProvider getRecordSetProvider()
    {
        return target.getTarget().getRecordSetProvider();
    }

    @Override
    public ConnectorPageSinkProvider getPageSinkProvider()
    {
        return target.getTarget().getPageSinkProvider();
    }

    @Override
    public ConnectorRecordSinkProvider getRecordSinkProvider()
    {
        return target.getTarget().getRecordSinkProvider();
    }

    @Override
    public Set<SystemTable> getSystemTables()
    {
        return target.getTarget().getSystemTables();
    }

    @Override
    public void shutdown()
    {
        target.getTarget().shutdown();
    }
}
