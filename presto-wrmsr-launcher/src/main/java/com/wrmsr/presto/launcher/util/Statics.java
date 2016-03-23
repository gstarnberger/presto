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
package com.wrmsr.presto.launcher.util;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Statics
{
    private static final List<String> HIDDEN_CLASSES = ImmutableList.<String>builder()
            .add("org.slf4j")
            .build();

    private static final ImmutableList<String> PARENT_FIRST_CLASSES = ImmutableList.<String>builder()
            .add("io.airlift.log.Logging")
            .build();

    public static void runStaticMethod(String className, String methodName, Class<?>[] parameterTypes, Object[] args)
    {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class cls = cl.loadClass(className);
            Method main = cls.getMethod(methodName, parameterTypes);
            main.invoke(null, args);
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static void runStaticMethod(List<URL> urls, String className, String methodName, Class<?>[] parameterTypes, Object[] args)
    {
        List<Throwable> unhandled = new CopyOnWriteArrayList<>();
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try {
                    // ClassLoader pcl = getContextClassLoader();
                    // ClassLoader cl = new ParentFirstClassLoader(urls, pcl, HIDDEN_CLASSES, PARENT_FIRST_CLASSES);
                    ClassLoader cl = new URLClassLoader(urls.toArray(new URL[urls.size()]), getContextClassLoader().getParent());

                    Thread.currentThread().setContextClassLoader(cl);
                    runStaticMethod(className, methodName, parameterTypes, args);
                }
                catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        };
        t.start();
        t.setUncaughtExceptionHandler((thr, e) -> unhandled.add(e));
        try {
            t.join();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (!unhandled.isEmpty()) {
            throw new RuntimeException(unhandled.get(0));
        }
    }
}
