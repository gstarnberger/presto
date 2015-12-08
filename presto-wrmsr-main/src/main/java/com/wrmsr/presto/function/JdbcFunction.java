package com.wrmsr.presto.function;

import com.facebook.presto.connector.ConnectorManager;
import com.facebook.presto.plugin.jdbc.BaseJdbcClient;
import com.facebook.presto.plugin.jdbc.JdbcConnector;
import com.facebook.presto.plugin.jdbc.JdbcMetadata;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.wrmsr.presto.util.jdbc.ScriptRunner;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;

import javax.inject.Inject;

import java.io.StringReader;
import java.lang.invoke.MethodHandle;
import java.sql.Connection;
import java.sql.SQLException;

// String conn, String... command
// FIXME use connectorSupport
// TODO jdbc_exec, jdbc_eval
// TODO init from file
public class JdbcFunction
    extends StringVarargsFunction
        implements FunctionRegistration.Self
{
    private final ConnectorManager connectorManager;

    @Inject
    public JdbcFunction(ConnectorManager connectorManager)
    {
        super(
                "jdbc",
                "execute raw jdbc statements",
                ImmutableList.of(),
                1,
                "varchar",
                "jdbc",
                ImmutableList.of(Context.class, Slice.class));
        this.connectorManager = connectorManager;
    }

    protected static class Context
    {
        public final ConnectorManager connectorManager;

        public Context(ConnectorManager connectorManager)
        {
            this.connectorManager = connectorManager;
        }
    }

    @Override
    protected MethodHandle bindMethodHandle()
    {
        return super.bindMethodHandle().bindTo(new Context(connectorManager));
    }

    public static Slice jdbc(Context context, Slice connectorName, Object[] commands)
    {
        JdbcConnector connector = (JdbcConnector) context.connectorManager.getConnectors().get(connectorName.toStringUtf8());
        JdbcMetadata metadata = (JdbcMetadata) connector.getMetadata();
        BaseJdbcClient client = (BaseJdbcClient) metadata.getJdbcClient();
        try (Connection connection = client.getConnection()) {
            for (int i = 0; i < commands.length; ++i) {
                String sql = ((Slice) commands[i]).toStringUtf8();
                ScriptRunner scriptRunner = new ScriptRunner(connection);
                scriptRunner.runScript(new StringReader(sql));
            }
        }
        catch (SQLException e) {
            throw Throwables.propagate(e);
        }
        return Slices.EMPTY_SLICE;
    }
}