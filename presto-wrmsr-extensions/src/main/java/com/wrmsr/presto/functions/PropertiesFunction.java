package com.wrmsr.presto.functions;

import com.facebook.presto.spi.block.BlockBuilder;
import com.facebook.presto.spi.block.BlockBuilderStatus;
import com.facebook.presto.spi.block.VariableWidthBlockBuilder;
import com.facebook.presto.spi.type.TypeManager;
import com.facebook.presto.spi.type.VarcharType;
import com.google.common.collect.ImmutableList;
import io.airlift.slice.Slice;

import java.lang.invoke.MethodHandle;
import java.util.List;

import static com.facebook.presto.type.TypeUtils.buildStructuralSlice;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;

public class PropertiesFunction
    extends StringVarargsFunction
{
    private final TypeManager typeManager;

    public PropertiesFunction(TypeManager typeManager)
    {
        super(
                "properties",
                "create a new properties map",
                ImmutableList.of(),
                2,
                "properties",
                "newProperties",
                ImmutableList.of(PropertiesFunction.class)
        );
        this.typeManager = typeManager;
    }

    @Override
    protected MethodHandle bindMethodHandle()
    {
        return super.bindMethodHandle().bindTo(this);
    }

    public static Slice newProperties(PropertiesFunction self, Object... strs)
    {
        checkArgument(strs.length % 2 == 0);
        BlockBuilder blockBuilder = new VariableWidthBlockBuilder(new BlockBuilderStatus());
        for (int i = 0; i < strs.length; i += 2) {
            Slice key = ((Slice) strs[i]);
            Slice value = ((Slice) strs[i+1]);

            checkArgument(key != null);
            blockBuilder.writeBytes(key, 0, key.length());
            blockBuilder.closeEntry();

            if (value == null) {
                blockBuilder.appendNull();
            }
            else {
                blockBuilder.writeBytes(value, 0, value.length());
                blockBuilder.closeEntry();
            }
        }
        return buildStructuralSlice(blockBuilder);
    }
}
