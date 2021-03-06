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
package com.wrmsr.presto.util;

import com.facebook.presto.spi.type.Type;
import io.airlift.slice.Slice;

import java.util.List;

public class Decoders
{
    private Decoders()
    {
    }

    public interface CodecField
    {
        String getName();

        Type getType();
    }

    public interface CodecSchema
    {
        List<CodecField> getFields();
    }

    public interface RowDecoder
    {

    }

    public interface FieldDecoder
    {
        Type getType(int field);

        boolean advanceNextPosition();

        boolean getBoolean(int field);

        long getLong(int field);

        double getDouble(int field);

        Slice getSlice(int field);

        boolean isNull(int field);
    }
}
