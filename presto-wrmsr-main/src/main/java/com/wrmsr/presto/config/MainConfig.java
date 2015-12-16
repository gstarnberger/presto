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
package com.wrmsr.presto.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.wrmsr.presto.util.config.mergeable.MergeableConfig;

import java.util.List;
import java.util.Map;

import static com.wrmsr.presto.util.Serialization.OBJECT_MAPPER;

public class MainConfig
        extends MergeableConfig<MainConfigNode>
{
    @JsonCreator
    public static MainConfig valueOf(List<Map<String, Object>> contents)
    {
        return new MainConfig(nodesFrom(OBJECT_MAPPER.get(), contents, MainConfigNode.class, UnknownConfig::new));
    }

    public MainConfig()
    {
        super(MainConfigNode.class);
    }

    public MainConfig(List<MainConfigNode> nodes)
    {
        super(nodes, MainConfigNode.class);
    }

    @JsonValue
    public List<Map<String, Object>> jsonValue()
    {
        return jsonValue(OBJECT_MAPPER.get());
    }
}
