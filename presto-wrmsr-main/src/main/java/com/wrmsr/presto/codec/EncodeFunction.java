package com.wrmsr.presto.codec;

import com.facebook.presto.metadata.FunctionRegistry;
import com.facebook.presto.metadata.Signature;
import com.facebook.presto.metadata.FunctionResolver;
import com.facebook.presto.metadata.SqlScalarFunction;
import com.facebook.presto.operator.scalar.ScalarFunctionImplementation;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.spi.type.TypeManager;
import com.facebook.presto.spi.type.TypeSignature;
import com.facebook.presto.sql.tree.QualifiedName;
import com.google.common.collect.ImmutableList;
import com.wrmsr.presto.function.FunctionRegistration;

import javax.annotation.Nullable;
import javax.inject.Inject;

import java.util.List;
import java.util.Map;

import static com.facebook.presto.metadata.Signature.typeParameter;

public class EncodeFunction
        extends SqlScalarFunction
        implements FunctionRegistration.Self, FunctionResolver
{
    public static final String NAME = "encode";

    private final TypeCodecProvider typeCodecProvider;

    @Inject
    public EncodeFunction(TypeCodecProvider typeCodecProvider)
    {
        super(NAME, ImmutableList.of(typeParameter("F"), typeParameter("T")), "T", ImmutableList.of("varchar", "F"));
        this.typeCodecProvider = typeCodecProvider;
    }

    @Nullable
    @Override
    public Signature resolveFunction(QualifiedName name, List<TypeSignature> parameterTypes, boolean approximate)
    {
        if (NAME.equals(name)) {
        }
        return null;
    }

    @Override
    public ScalarFunctionImplementation specialize(Map<String, Type> types, int arity, TypeManager typeManager, FunctionRegistry functionRegistry)
    {
//        typeCodecManager.getTypeCodec()
        return null;
    }

    @Override
    public boolean isHidden()
    {
        return false;
    }

    @Override
    public boolean isDeterministic()
    {
        return true;
    }

    @Override
    public String getDescription()
    {
        return "encode";
    }
}
