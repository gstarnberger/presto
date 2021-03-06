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
package com.wrmsr.presto.struct;

import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.type.RowType;
import com.wrmsr.presto.codec.TypeCodec;
import com.wrmsr.presto.util.codec.Codec;

import static com.google.common.base.Preconditions.checkArgument;

public class FlatTypeCodec
        extends TypeCodec<Block>
{
    public FlatTypeCodec()
    {
        super("flat", Block.class);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public <T> Specialization<T, Block> specialize(Type fromType)
    {
        checkArgument(fromType instanceof RowType);  // FIXME
        return new Specialization(Codec.identity(), fromType, true);
    }
}
