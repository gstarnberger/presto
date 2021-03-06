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
package com.wrmsr.presto.signal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.wrmsr.presto.config.Config;

import java.util.List;

public class SignalConfig
    implements Config<SignalConfig>
{
    private List<String> shutdownSignals = ImmutableList.of();

    @JsonProperty("shutdown-signals")
    public List<String> getShutdownSignals()
    {
        return shutdownSignals;
    }

    @JsonProperty("shutdown-signals")
    public void setShutdownSignals(List<String> shutdownSignals)
    {
        this.shutdownSignals = shutdownSignals;
    }
}
