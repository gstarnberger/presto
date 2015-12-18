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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wrmsr.presto.launcher.cluster.ClustersConfig;
import com.wrmsr.presto.util.config.mergeable.MergeableConfig;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_OBJECT
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClustersConfig.class, name = "clusters"),
        @JsonSubTypes.Type(value = EnvConfig.class, name = "env"),
        @JsonSubTypes.Type(value = JvmConfig.class, name = "jvm"),
        @JsonSubTypes.Type(value = LauncherConfig.class, name = "launcher"),
        @JsonSubTypes.Type(value = LogConfig.class, name = "log"),
        @JsonSubTypes.Type(value = SystemConfig.class, name = "system"),
})
public interface Config<N extends Config<N>>
    extends MergeableConfig<N>
{
}
