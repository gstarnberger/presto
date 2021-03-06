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
package com.wrmsr.presto.hive;

import com.facebook.presto.hive.HiveConnector;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.connector.Connector;
import com.wrmsr.presto.spi.connectorSupport.EvalConnectorSupport;

import java.util.List;

public class HiveConnectorSupport
    implements EvalConnectorSupport
{
    private final ConnectorSession session;
    private final HiveConnector connector;

    public HiveConnectorSupport(ConnectorSession session, Connector connector)
    {
        this.session = session;
        this.connector = (HiveConnector) connector;
    }

    @Override
    public Connector getConnector()
    {
        return null;
    }

    @Override
    public List eval(String cmd)
    {
        return null;
    }
}
