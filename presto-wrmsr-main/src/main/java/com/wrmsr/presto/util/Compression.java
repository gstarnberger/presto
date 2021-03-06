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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.wrmsr.presto.util.codec.Codec;
import com.wrmsr.presto.util.codec.StreamCodec;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compression
{
    private Compression()
    {
    }

    public interface CompressionCodec
            extends Codec<byte[], byte[]>
    {
    }

    public interface CompressionStreamCodec
            extends StreamCodec
    {
    }

    public static class ZlibCodec
            implements CompressionCodec
    {
        private static final int BUFFER_SIZE = 1024;

        @Override
        public byte[] decode(byte[] data)
        {
            try {
                Inflater inflater = new Inflater();
                inflater.setInput(data);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
                byte[] buffer = new byte[BUFFER_SIZE];
                while (!inflater.finished()) {
                    int count = inflater.inflate(buffer);
                    outputStream.write(buffer, 0, count);
                }
                outputStream.close();
                return outputStream.toByteArray();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public byte[] encode(byte[] data)
        {
            try {
                Deflater deflater = new Deflater();
                deflater.setInput(data);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
                byte[] buffer = new byte[BUFFER_SIZE];
                while (!deflater.finished()) {
                    int count = deflater.deflate(buffer);
                    outputStream.write(buffer, 0, count);
                }
                outputStream.close();
                return outputStream.toByteArray();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class CommonsCompressionStreamCodec
            implements CompressionStreamCodec
    {
        private final String name;

        public CommonsCompressionStreamCodec(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public String toString()
        {
            return "CommonsCompressionCodec{" +
                    "name='" + name + '\'' +
                    '}';
        }

        @Override
        public InputStream decode(InputStream data)
        {
            try {
                return new CompressorStreamFactory().createCompressorInputStream(name, data);
            }
            catch (CompressorException e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public OutputStream encode(OutputStream data)
        {
            try {
                return new CompressorStreamFactory().createCompressorOutputStream(name, data);
            }
            catch (CompressorException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    public static final Map<String, CompressionStreamCodec> COMPRESSION_STREAM_CODECS_BY_NAME;

    static {
        ImmutableMap.Builder<String, CompressionStreamCodec> builder = ImmutableMap.builder();
        // builder.put("zlib", new ZlibCodec());
        for (String name : new String[] {
                CompressorStreamFactory.BZIP2,
                CompressorStreamFactory.GZIP,
                CompressorStreamFactory.PACK200,
                CompressorStreamFactory.XZ,
                CompressorStreamFactory.LZMA,
                CompressorStreamFactory.SNAPPY_FRAMED,
                CompressorStreamFactory.SNAPPY_RAW,
                CompressorStreamFactory.Z,
                CompressorStreamFactory.DEFLATE
        }) {
            builder.put(name, new CommonsCompressionStreamCodec(name));
        }
        COMPRESSION_STREAM_CODECS_BY_NAME = builder.build();
    }

    public static class CommonsCompressionCodec implements CompressionCodec
    {
        private final String name;

        public CommonsCompressionCodec(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public byte[] encode(byte[] data)
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (CompressorOutputStream out = new CompressorStreamFactory().createCompressorOutputStream(name, baos)) {
                out.write(data);
            }
            catch (CompressorException | IOException e) {
                throw Throwables.propagate(e);
            }
            return baos.toByteArray();
        }

        @Override
        public byte[] decode(byte[] data)
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            try (CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(name, bais)) {
                return IOUtils.toByteArray(in);
            }
            catch (CompressorException | IOException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    public static final Set<String> COMMONS_COMPRESSION_NAMES = ImmutableSet.of(
            CompressorStreamFactory.GZIP,
            CompressorStreamFactory.BZIP2,
            CompressorStreamFactory.XZ,
            CompressorStreamFactory.PACK200,
            CompressorStreamFactory.DEFLATE
    );
}
