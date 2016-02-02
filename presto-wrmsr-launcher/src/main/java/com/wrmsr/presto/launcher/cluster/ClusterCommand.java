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
package com.wrmsr.presto.launcher.cluster;

import com.wrmsr.presto.launcher.AbstractLauncherCommand;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Command(name = "cluster", description = "Runs cluster command")
public final class ClusterCommand
        extends AbstractLauncherCommand
{
    @Arguments(description = "arguments")
    private List<String> args = newArrayList();

    @Override
    public void run()
    {
        String[] args = this.args.toArray(new String[this.args.size()]);
        // ClusterCommands.main(this, args);
    }
}
