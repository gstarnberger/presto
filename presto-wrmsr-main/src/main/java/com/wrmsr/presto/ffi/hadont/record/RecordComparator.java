/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wrmsr.presto.ffi.hadont.record;

import com.wrmsr.presto.ffi.hadont.io.WritableComparable;
import com.wrmsr.presto.ffi.hadont.io.WritableComparator;

/**
 * A raw record comparator base class
 *
 * @deprecated Replaced by <a href="http://hadoop.apache.org/avro/">Avro</a>.
 */
@Deprecated
public abstract class RecordComparator
        extends WritableComparator
{

    /**
     * Construct a raw {@link Record} comparison implementation.
     */
    protected RecordComparator(Class<? extends WritableComparable> recordClass)
    {
        super(recordClass);
    }

    // inheric JavaDoc
    @Override
    public abstract int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2);

    /**
     * Register an optimized comparator for a {@link Record} implementation.
     *
     * @param c record classs for which a raw comparator is provided
     * @param comparator Raw comparator instance for class c
     */
    public static synchronized void define(Class c, RecordComparator comparator)
    {
        WritableComparator.define(c, comparator);
    }
}
