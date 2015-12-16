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
package com.wrmsr.presto.launcher.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.presto.util.config.Configs;
import com.wrmsr.presto.util.config.mergeable.StringMapMergeableConfig;

import java.util.Map;

public final class SystemConfig
        extends StringMapMergeableConfig<SystemConfig>
        implements Config<SystemConfig>
{
    @JsonCreator
    public static SystemConfig valueOf(Object object)
    {
        return new SystemConfig(Configs.flatten(object));
    }

    public SystemConfig(Map<String, String> entries)
    {
        super(entries);
    }

    public SystemConfig()
    {
        this(ImmutableMap.of());
    }
}
