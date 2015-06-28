package com.wrmsr.presto.functions;

import com.facebook.presto.Session;
import com.facebook.presto.metadata.*;
import com.facebook.presto.operator.scalar.ScalarFunction;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.type.StandardTypes;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.spi.type.TypeManager;
import com.facebook.presto.sql.analyzer.*;
import com.facebook.presto.sql.parser.ParsingException;
import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.planner.optimizations.PlanOptimizer;
import com.facebook.presto.sql.tree.Statement;
import com.facebook.presto.type.RowType;
import com.facebook.presto.type.SqlType;
import com.google.common.collect.ImmutableList;
import io.airlift.slice.Slice;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.facebook.presto.metadata.Signature.comparableTypeParameter;
import static com.facebook.presto.spi.type.TypeSignature.parseTypeSignature;
import static com.facebook.presto.type.TypeUtils.parameterizedTypeName;
import static com.facebook.presto.util.Reflection.methodHandle;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.wrmsr.presto.util.ImmutableCollectors.toImmutableList;
import static com.facebook.presto.spi.StandardErrorCode.INTERNAL_ERROR;
import static com.facebook.presto.spi.type.TimeZoneKey.UTC_KEY;
import static java.util.Locale.ENGLISH;

public class DefineStructForQueryFunction
        extends ParametricScalar
{
    private static final Signature SIGNATURE = new Signature(
            "define_struct_for_query", ImmutableList.of(comparableTypeParameter("varchar")), StandardTypes.VARCHAR, ImmutableList.of(StandardTypes.VARCHAR, StandardTypes.VARCHAR), false, false);
    private static final MethodHandle METHOD_HANDLE = methodHandle(DefineStructForQueryFunction.class, "defineStructForQuery", DefineStructForQueryFunction.class, ConnectorSession.class, Slice.class, Slice.class);

    private final SqlParser sqlParser;
    private final List<PlanOptimizer> planOptimizers;
    private final boolean experimentalSyntaxEnabled;
    private final StructManager structManager;
    private final Metadata metadata;

    public DefineStructForQueryFunction(StructManager structManager, SqlParser sqlParser, List<PlanOptimizer> planOptimizers, FeaturesConfig featuresConfig, Metadata metadata)
    {
        this.structManager = structManager;
        this.sqlParser = checkNotNull(sqlParser);
        this.planOptimizers = checkNotNull(planOptimizers);
        checkNotNull(featuresConfig, "featuresConfig is null");
        this.experimentalSyntaxEnabled = featuresConfig.isExperimentalSyntaxEnabled();
        this.metadata = metadata;
    }

    public Analysis analyzeStatement(Statement statement, Session session)
    {
        QueryExplainer explainer = new QueryExplainer(session, planOptimizers, metadata, sqlParser, experimentalSyntaxEnabled);
        Analyzer analyzer = new Analyzer(session, metadata, sqlParser, Optional.of(explainer), experimentalSyntaxEnabled);
        return analyzer.analyze(statement);
    }

    @Override
    public Signature getSignature()
    {
        return SIGNATURE;
    }

    @Override
    public boolean isHidden()
    {
        return false;
    }

    @Override
    public boolean isDeterministic()
    {
        return false;
    }

    @Override
    public String getDescription()
    {
        return "defines a new struct type for the results of a given query";
    }

    @Nullable
    public RowType buildRowType(String name, String sql)
    {
        Session.SessionBuilder builder = Session.builder()
                .setUser("system")
                .setSource("system")
                .setCatalog("default")
                .setTimeZoneKey(UTC_KEY)
                .setLocale(ENGLISH)
                .setSchema("default");
        Session session = builder.build();
        return buildRowType(session, name, sql);
    }

    @Nullable
    public RowType buildRowType(Session session, String name, String sql)
    {
        checkArgument(name.toLowerCase().equals(name));

        // verify round-trip
        Statement statement;
        try {
            statement = sqlParser.createStatement(sql);
        }
        catch (ParsingException e) {
            throw new PrestoException(INTERNAL_ERROR, "Formatted query does not parse: " + sql);
        }

        Analysis analysis = analyzeStatement(statement, session);
        TupleDescriptor tupleDescriptor;

        try {
            tupleDescriptor = analysis.getOutputDescriptor();
        }
        catch (SemanticException e) {
            if (e.getCode() == SemanticErrorCode.MISSING_TABLE) {
                return null;
            }
            else {
                throw e;
            }
        }

        Collection<Field> visibleFields = tupleDescriptor.getVisibleFields();
        List<Type> fieldTypes = visibleFields.stream().map(f -> f.getType()).collect(toImmutableList());
        List<Optional<String>> fieldNameOptions = visibleFields.stream().map(f -> f.getName()).collect(toImmutableList());
        long numNamed = fieldNameOptions.stream().filter(o -> o.isPresent()).count();
        Optional<List<String>> fieldNames;
        if (numNamed == (long) fieldNameOptions.size()) {
            fieldNames = Optional.of(fieldNameOptions.stream().map(o -> o.get()).collect(toImmutableList()));
        }
        else if (numNamed == 0) {
            fieldNames = Optional.empty();
        }
        else {
            throw new RuntimeException(String.format("All fields must be named or no fields must be named for type: %s -> %s", name, sql));
        }

        return new RowType(parameterizedTypeName(name), fieldTypes, fieldNames);
    }

    @Override
    public FunctionInfo specialize(Map<String, Type> types, int arity, TypeManager typeManager, FunctionRegistry functionRegistry)
    {
        checkArgument(arity == 2);
        checkArgument(types.size() == 1);

        return new FunctionInfo(
                new Signature(
                        "define_struct_for_query",
                        StandardTypes.VARCHAR,
                        ImmutableList.of(StandardTypes.VARCHAR, StandardTypes.VARCHAR)),
                getDescription(),
                isHidden(),
                METHOD_HANDLE.bindTo(this),
                isDeterministic(),
                true,
                ImmutableList.of(false, false));
    }

    public static Slice defineStructForQuery(DefineStructForQueryFunction self, ConnectorSession session, Slice name, Slice query)
    {
        return null;
    }

    /*
    public static Slice defineStructForQuery(DefineStructForQueryFunction self, Slice name, Object... strs)
    {
        checkArgument(strs.length % 2 == 0);
        List<StructManager.StructDefinition.Field> fields = newArrayList();
        for (int i = 0; i < strs.length; i += 2) {
            fields.add(new StructManager.StructDefinition.Field(((Slice) strs[i]).toStringUtf8(), ((Slice) strs[i + 1]).toStringUtf8()));
        }
        StructManager.StructDefinition def = new StructManager.StructDefinition(name.toStringUtf8(), fields);
        context.structManager.registerStruct(context.structManager.buildRowType(def));
        return name;
    }
    */
}