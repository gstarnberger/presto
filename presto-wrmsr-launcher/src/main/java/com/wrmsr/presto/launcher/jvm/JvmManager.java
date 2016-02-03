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
package com.wrmsr.presto.launcher.jvm;

import com.google.common.collect.ImmutableList;
import com.sun.management.OperatingSystemMXBean;
import com.wrmsr.presto.launcher.LauncherFailureException;
import com.wrmsr.presto.launcher.util.JvmConfiguration;
import io.airlift.log.Logger;
import io.airlift.units.DataSize;
import jnr.posix.POSIX;

import javax.inject.Inject;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.wrmsr.presto.util.collect.ImmutableCollectors.toImmutableList;
import static java.util.Objects.requireNonNull;

public class JvmManager
{
    private static final Logger log = Logger.get(JvmManager.class);

    private final JvmConfig jvmConfig;
    private final Jvm jvm;
    private final POSIX posix;

    @Inject
    public JvmManager(JvmConfig jvmConfig, Jvm jvm, POSIX posix)
    {
        this.jvmConfig = requireNonNull(jvmConfig);
        this.jvm = jvm;
        this.posix = posix;
    }

    public List<String> getConfigJvmOptions()
    {
        ImmutableList.Builder<String> builder = ImmutableList.builder();

        JvmConfig.DebugConfig debug = jvmConfig.getDebug();
        if (debug != null && debug.getPort() != null) {
            builder.add(JvmConfiguration.DEBUG.valueOf().toString());
            builder.add(JvmConfiguration.REMOTE_DEBUG.valueOf(debug.getPort(), debug.isSuspend()).toString());
        }

        if (jvmConfig.getHeap() != null) {
            JvmConfig.HeapConfig heap = jvmConfig.getHeap();
            checkArgument(!isNullOrEmpty(heap.getSize()));
            OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            long base;
            if (heap.isFree()) {
                base = os.getFreePhysicalMemorySize();
            }
            else {
                base = os.getTotalPhysicalMemorySize();
            }

            long sz;
            if (heap.getSize().endsWith("%")) {
                sz = ((base * 100) / Integer.parseInt(heap.getSize())) / 100;
            }
            else {
                boolean negative = heap.getSize().startsWith("-");
                sz = (long) DataSize.valueOf(heap.getSize().substring(negative ? 1 : 0)).getValue(DataSize.Unit.BYTE);
                if (negative) {
                    sz = base - sz;
                }
            }

            if (heap.getMax() != null) {
                long max = (long) heap.getMax().getValue(DataSize.Unit.BYTE);
                if (sz > max) {
                    sz = max;
                }
            }
            if (heap.getMin() != null) {
                long min = (long) heap.getMin().getValue(DataSize.Unit.BYTE);
                if (sz < min) {
                    if (heap.isAttempt()) {
                        sz = min;
                    }
                    else {
                        throw new LauncherFailureException(String.format("Insufficient memory: got %d, need %d", sz, min));
                    }
                }
            }
            checkArgument(sz > 0);
            builder.add(JvmConfiguration.MAX_HEAP_SIZE.valueOf(new DataSize(sz, DataSize.Unit.BYTE)).toString());
        }

        if (jvmConfig.getGc() != null) {
            JvmConfig.GcConfig gc = jvmConfig.getGc();
            if (gc instanceof JvmConfig.G1GcConfig) {
                builder.add("-XX:+UseG1GC");
            }
            else if (gc instanceof JvmConfig.CmsGcConfig) {
                builder.add("-XX:+UseConcMarkSweepGC");
            }
            else {
                throw new IllegalArgumentException(gc.toString());
            }
        }

        builder.addAll(jvmConfig.getOptions());

        return builder.build();
    }

    public List<String> getOverridenJvmOptions()
    {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMxBean.getInputArguments();
    }

    public String formatSystemPropertyOption(String key, String value)
    {
        return "-D" + requireNonNull(key) + "=" + requireNonNull(value);
    }

    public List<String> formatSystemPropertyOptions(Map<String, String>... propertyMaps)
    {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (Map<String, String> properties : Arrays.asList(propertyMaps)) {
            builder.addAll(properties.entrySet().stream().map(e -> formatSystemPropertyOption(e.getKey(), e.getValue())).collect(toImmutableList()));
        }
        return builder.build();
    }

    public void exec(List<String> args)
    {
        File jvm = this.jvm.getValue();

        ImmutableList.Builder<String> builder = ImmutableList.<String>builder()
                .add(jvm.getAbsolutePath())
                .addAll(getConfigJvmOptions());

        List<String> newArgs = builder.build();
        posix.libc().execv(jvm.getAbsolutePath(), newArgs.toArray(new String[newArgs.size()]));
        log.error("Exec failed");
        System.exit(1);
    }
}
