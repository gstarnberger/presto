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

import com.wrmsr.presto.util.Mergeable;

public abstract class AttributeMap<M extends AttributeMap<M, A>, A extends Attribute>
        extends ClassInstanceMap<A>
        implements Mergeable<M>
{
    public AttributeMap(Class<? extends A> cls)
    {
        super(cls);
    }

    public M merge(M other)
    {
        throw new IllegalStateException(); // FIXME
    }
}
