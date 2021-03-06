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
package com.wrmsr.presto.function.bitwise;

import com.facebook.presto.operator.aggregation.AggregationFunction;
import com.facebook.presto.operator.aggregation.CombineFunction;
import com.facebook.presto.operator.aggregation.InputFunction;
import com.facebook.presto.operator.aggregation.OutputFunction;
import com.facebook.presto.operator.aggregation.state.NullableLongState;
import com.facebook.presto.spi.block.BlockBuilder;
import com.facebook.presto.spi.type.BigintType;
import com.facebook.presto.spi.type.StandardTypes;
import com.facebook.presto.type.SqlType;

@AggregationFunction(BitAndFunction.NAME + "_agg")
public class BitOrAggregationFunction
{
    @InputFunction
    public static void input(NullableLongState state, @SqlType(StandardTypes.BIGINT) long value)
    {
        if (state.isNull()) {
            state.setNull(false);
            state.setLong(value);
        }
        else {
            state.setLong(value | state.getLong());
        }
    }

    @CombineFunction
    public static void combine(NullableLongState state, NullableLongState otherState)
    {
        if (!otherState.isNull()) {
            if (state.isNull()) {
                state.setNull(false);
                state.setLong(otherState.getLong());
            }
            else {
                state.setLong(otherState.getLong() | state.getLong());
            }
        }
    }

    @OutputFunction(StandardTypes.BIGINT)
    public static void output(NullableLongState state, BlockBuilder out)
    {
        if (state.isNull()) {
            out.appendNull();
        }
        else {
            BigintType.BIGINT.writeLong(out, state.getLong());
        }
    }
}
