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
package com.wrmsr.presto.metaconnectors.codec.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.presto.metaconnectors.codec.CodecColumnHandle;
import com.wrmsr.presto.metaconnectors.codec.FieldValueProvider;
import io.airlift.slice.Slice;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Custom date format decoder.
 * <p/>
 * <tt>formatHint</tt> uses {@link DateTimeFormatter} format.
 * <p/>
 * Uses hardcoded UTC timezone and english locale.
 */
public class CustomDateTimeJsonFieldCodec
        extends JsonFieldCodec
{
    @Override
    public Set<Class<?>> getJavaTypes()
    {
        return ImmutableSet.<Class<?>>of(long.class, Slice.class);
    }

    @Override
    public String getFieldDecoderName()
    {
        return "custom-date-time";
    }

    @Override
    public FieldValueProvider decode(JsonNode value, CodecColumnHandle columnHandle)
    {
        checkNotNull(columnHandle, "columnHandle is null");
        checkNotNull(value, "value is null");

        return new CustomDateTimeJsonValueProvider(value, columnHandle);
    }

    public static class CustomDateTimeJsonValueProvider
            extends DateTimeJsonValueProvider
    {
        public CustomDateTimeJsonValueProvider(JsonNode value, CodecColumnHandle columnHandle)
        {
            super(value, columnHandle);
        }

        @Override
        protected long getMillis()
        {
            if (isNull()) {
                return 0L;
            }

            if (value.canConvertToLong()) {
                return value.asLong();
            }

            checkNotNull(columnHandle.getFormatHint(), "formatHint is null");
            String textValue = value.isValueNode() ? value.asText() : value.toString();

            DateTimeFormatter formatter = DateTimeFormat.forPattern(columnHandle.getFormatHint()).withLocale(Locale.ENGLISH).withZoneUTC();
            return formatter.parseMillis(textValue);
        }
    }
}
