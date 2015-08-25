/*
 * Copyright 2011 Proofpoint, Inc.
 *
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
package com.proofpoint.hive.serde;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CaseInsensitiveMap<V>
        implements Map<String, V>
{
    Map<String, V> map = new LinkedHashMap<String, V>();

    @Override
    public int size()
    {
        return map.size();
    }

    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return map.containsKey(convertKey(key));
    }

    @Override
    public boolean containsValue(Object value)
    {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key)
    {
        return map.get(convertKey(key));
    }

    @Override
    public V remove(Object key)
    {
        return map.remove(convertKey(key));
    }

    @Override
    public void clear()
    {
        map.clear();
    }

    @Override
    public Set<String> keySet()
    {
        return map.keySet();
    }

    @Override
    public Collection<V> values()
    {
        return map.values();
    }

    @Override
    public Set<Entry<String, V>> entrySet()
    {
        return map.entrySet();
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m)
    {
        for (Entry<? extends String, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V put(String key, V value)
    {
        return map.put(convertKey(key), value);
    }

    private static String convertKey(Object key)
    {
        return ((String) key).toLowerCase();
    }
}
