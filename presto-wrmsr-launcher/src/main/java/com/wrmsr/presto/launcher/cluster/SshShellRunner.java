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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.leacox.process.FinalizedProcess;
import com.leacox.process.FinalizedProcessBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

public class SshShellRunner
        extends ShellRunner
{
    private List<String> buildTargetArgs(Target target)
    {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        if (target.getAuth() instanceof IdentityFileAuth) {
            builder.add("-i", new File(((IdentityFileAuth) target.getAuth()).getIdentityFile()).getAbsolutePath());
        }
        else {
            throw new IllegalArgumentException(Objects.toString(target.getAuth()));
        }
        return builder.build();
    }

    private final long timeout;

    public SshShellRunner(long timeout)
    {
        this.timeout = timeout;
    }

    public void syncDirectoriesScp(Target target, File local, String remote)
    {
        checkArgument(!remote.contains("..") && !remote.startsWith("/"));
        runCommand(target, "rm", "-rf", remote);
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add("scp", "-r");
        builder.add("-P", Integer.toString(target.getPort()));
        builder.addAll(buildTargetArgs(target));
        builder.add(local.getAbsolutePath());
        builder.add(String.format("%s@%s:%s", target.getUser(), target.getHost(), remote));

        FinalizedProcessBuilder pb = new FinalizedProcessBuilder(builder.build())
                .gobbleInputStream(true)
                .gobbleErrorStream(true);
        try (FinalizedProcess process = pb.start()) {
            if (process.waitFor(timeout) != 0) {
                throw new RuntimeException("Failed to send file");
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException();
        }
        catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void syncDirectories(Target target, File local, String remote)
    {
        syncDirectoriesScp(target, local, remote);
    }

    @Override
    protected List<String> buildRunCommandArgs(Target target, String command, String... args)
    {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add("ssh", "-t");
        builder.add("-p", Integer.toString(target.getPort()));
        builder.add(String.format("%s@%s", target.getUser(), target.getHost()));
        builder.addAll(buildTargetArgs(target));
        builder.addAll(buildTrailingRunCommandArgs(target, command, args));
        return builder.build();
    }

    public static void main(String[] args)
            throws Throwable
    {
        new SshShellRunner(60 * 1000)
                .runCommand(
                        new Target(
                                "dev8-devc",
                                22,
                                "wtimoney",
                                new IdentityFileAuth(System.getProperty("user.home") + "/.ssh/id_rsa"),
                                "presto"),
                        "touch", "hi");
    }
}
