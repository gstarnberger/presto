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
package com.wrmsr.presto.reactor;

import com.facebook.presto.Session;
import com.facebook.presto.spi.ColumnHandle;
import com.facebook.presto.spi.ColumnMetadata;
import com.facebook.presto.spi.Connector;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.ConnectorTableHandle;
import com.facebook.presto.spi.SchemaTableName;
import com.facebook.presto.sql.planner.Plan;
import com.facebook.presto.sql.planner.PlanNodeIdAllocator;
import com.facebook.presto.sql.planner.Symbol;
import com.facebook.presto.sql.planner.SymbolAllocator;
import com.facebook.presto.sql.planner.plan.PlanNode;
import com.facebook.presto.sql.planner.plan.PlanNodeId;
import com.facebook.presto.sql.planner.plan.SimplePlanRewriter;
import com.facebook.presto.sql.planner.plan.TableScanNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.wrmsr.presto.util.ImmutableCollectors.toImmutableMap;

public class TestPkThreader
{
    public final TestHelper helper = new TestHelper();

    public static class PkThreader extends SimplePlanRewriter<PkThreader.Context>
    {
        private final PlanNodeIdAllocator idAllocator;
        private final SymbolAllocator symbolAllocator;
        private final Session session;
        private final Map<String, ConnectorSupport> connectorSupport;

        public PkThreader(PlanNodeIdAllocator idAllocator, SymbolAllocator symbolAllocator, Session session, Map<String, ConnectorSupport> connectorSupport)
        {
            this.idAllocator = idAllocator;
            this.symbolAllocator = symbolAllocator;
            this.session = session;
            this.connectorSupport = connectorSupport;
        }

        public static class Context
        {
            private final Map<PlanNodeId, List<Symbol>> nodePkSyms;

            public Context()
            {
                nodePkSyms = new HashMap<>();
            }
        }

        @Override
        public PlanNode visitTableScan(TableScanNode node, RewriteContext<Context> context)
        {
            // FIXME filter extraction
            ConnectorSupport cs = connectorSupport.get(node.getTable().getConnectorId());
            Connector c = cs.getConnector();
            ConnectorSession csess = session.toConnectorSession();
            SchemaTableName stn = cs.getSchemaTableName(node.getTable().getConnectorHandle());
            ConnectorTableHandle th = c.getMetadata().getTableHandle(csess, stn);
            Map<String, ColumnHandle> chs = c.getMetadata().getColumnHandles(csess, th);

            List<String> pkCols = cs.getPrimaryKey(stn);
            Map<String, Symbol> colSyms = node.getAssignments().entrySet().stream().map(e -> ImmutablePair.of(cs.getColumnName(e.getValue()), e.getKey())).collect(toImmutableMap());

            Map<Symbol, ColumnHandle> newAssignments = new HashMap<>(node.getAssignments());
            List<Symbol> newOutputSymbols = new ArrayList<>(node.getOutputSymbols());
            List<Symbol> pkSyms = new ArrayList<>();

            for (String pkCol : pkCols) {
                if (colSyms.containsKey(pkCol)) {
                    pkSyms.add(colSyms.get(pkCol));
                }
                else {
                    ColumnHandle ch = chs.get(pkCol);
                    ColumnMetadata cm = c.getMetadata().getColumnMetadata(csess, th, ch);
                    Symbol pkSym = symbolAllocator.newSymbol(pkCol, cm.getType());

                    newAssignments.put(pkSym, ch);
                    newOutputSymbols.add(pkSym);
                    pkSyms.add(pkSym);
                }
            }

            TableScanNode newNode = new TableScanNode(
                    node.getId(),
                    node.getTable(),
                    newOutputSymbols,
                    newAssignments,
                    node.getLayout(),
                    node.getCurrentConstraint(),
                    node.getOriginalConstraint()
            );
            context.get().nodePkSyms.put(newNode.getId(), pkSyms);
            return newNode;
        }
    }

    @Test
    public void testThing()
            throws Throwable
    {
        TestHelper.PlannedQuery pq = helper.plan("select name from tpch.tiny.customer");
        PkThreader.Context ctx = new PkThreader.Context();
        PkThreader r = new PkThreader(
                pq.idAllocator,
                pq.planner.getSymbolAllocator(),
                pq.session,
                pq.connectorSupport
        );
        PlanNode newRoot = SimplePlanRewriter.rewriteWith(r, pq.plan.getRoot(), ctx);
        System.out.println(newRoot);
        System.out.println(ctx);
    }
}
