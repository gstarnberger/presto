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
package com.wrmsr.presto.type;

import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.block.BlockBuilder;
import com.facebook.presto.spi.block.BlockBuilderStatus;
import com.facebook.presto.spi.block.InterleavedBlockBuilder;
import com.facebook.presto.spi.type.TypeManager;
import com.facebook.presto.spi.type.VarcharType;
import com.google.common.collect.ImmutableList;
import com.wrmsr.presto.function.FunctionRegistration;
import com.wrmsr.presto.function.StringVarargsFunction;
import io.airlift.slice.Slice;

import javax.inject.Inject;

import java.lang.invoke.MethodHandle;

import static com.google.common.base.Preconditions.checkArgument;

public class PropertiesFunction
        extends StringVarargsFunction
        implements FunctionRegistration.Self
{
    private final TypeManager typeManager;

    @Inject
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

    public static Block newProperties(PropertiesFunction self, Slice[] strs)
    {
        checkArgument(strs.length % 2 == 0);
        BlockBuilder blockBuilder = new InterleavedBlockBuilder(ImmutableList.of(VarcharType.VARCHAR, VarcharType.VARCHAR), new BlockBuilderStatus(), strs.length / 2);
        for (int i = 0; i < strs.length; i += 2) {
            Slice key = ((Slice) strs[i]);
            Slice value = ((Slice) strs[i + 1]);

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
        return blockBuilder.build();
    }
}
