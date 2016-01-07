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
package com.facebook.presto.metadata;

import com.facebook.presto.spi.ConnectorOutputTableHandle;
import com.facebook.presto.transaction.TransactionHandle;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class OutputTableHandle
{
    private final String connectorId;
    private final TransactionHandle transactionHandle;
    private final ConnectorOutputTableHandle connectorHandle;

    @JsonCreator
    public OutputTableHandle(
            @JsonProperty("connectorId") String connectorId,
            @JsonProperty("transactionHandle") TransactionHandle transactionHandle,
            @JsonProperty("connectorHandle") ConnectorOutputTableHandle connectorHandle)
    {
        this.connectorId = requireNonNull(connectorId, "connectorId is null");
        this.transactionHandle = requireNonNull(transactionHandle, "transactionHandle is null");
        this.connectorHandle = requireNonNull(connectorHandle, "connectorHandle is null");
    }

    @JsonProperty
    public String getConnectorId()
    {
        return connectorId;
    }

    @JsonProperty
    public TransactionHandle getTransactionHandle()
    {
        return transactionHandle;
    }

    @JsonProperty
    public ConnectorOutputTableHandle getConnectorHandle()
    {
        return connectorHandle;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(connectorId, transactionHandle, connectorHandle);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        OutputTableHandle other = (OutputTableHandle) obj;
        return Objects.equals(this.connectorId, other.connectorId) &&
                Objects.equals(this.transactionHandle, other.transactionHandle) &&
                Objects.equals(this.connectorHandle, other.connectorHandle);
    }

    @Override
    public String toString()
    {
        return connectorId + ":" + transactionHandle + ":" + connectorHandle;
    }
}
