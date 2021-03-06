/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wrmsr.presto.ffi.hadont.streaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

/**
 * Utilities used in streaming
 */
public class StreamUtil
{
    public static String findInClasspath(String className)
    {
        return findInClasspath(className, StreamUtil.class.getClassLoader());
    }

    /**
     * @return a jar file path or a base directory or null if not found.
     */
    public static String findInClasspath(String className, ClassLoader loader)
    {

        String relPath = className;
        relPath = relPath.replace('.', '/');
        relPath += ".class";
        java.net.URL classUrl = loader.getResource(relPath);

        String codePath;
        if (classUrl != null) {
            boolean inJar = classUrl.getProtocol().equals("jar");
            codePath = classUrl.toString();
            if (codePath.startsWith("jar:")) {
                codePath = codePath.substring("jar:".length());
            }
            if (codePath.startsWith("file:")) { // can have both
                codePath = codePath.substring("file:".length());
            }
            if (inJar) {
                // A jar spec: remove class suffix in /path/my.jar!/package/Class
                int bang = codePath.lastIndexOf('!');
                codePath = codePath.substring(0, bang);
            }
            else {
                // A class spec: remove the /my/package/Class.class portion
                int pos = codePath.lastIndexOf(relPath);
                if (pos == -1) {
                    throw new IllegalArgumentException("invalid codePath: className=" + className
                            + " codePath=" + codePath);
                }
                codePath = codePath.substring(0, pos);
            }
        }
        else {
            codePath = null;
        }
        return codePath;
    }

    static String qualifyHost(String url)
    {
        try {
            return qualifyHost(new URL(url)).toString();
        }
        catch (IOException io) {
            return url;
        }
    }

    static URL qualifyHost(URL url)
    {
        try {
            InetAddress a = InetAddress.getByName(url.getHost());
            String qualHost = a.getCanonicalHostName();
            URL q = new URL(url.getProtocol(), qualHost, url.getPort(), url.getFile());
            return q;
        }
        catch (IOException io) {
            return url;
        }
    }

    static final String regexpSpecials = "[]()?*+|.!^-\\~@";

    public static String regexpEscape(String plain)
    {
        StringBuffer buf = new StringBuffer();
        char[] ch = plain.toCharArray();
        int csup = ch.length;
        for (int c = 0; c < csup; c++) {
            if (regexpSpecials.indexOf(ch[c]) != -1) {
                buf.append("\\");
            }
            buf.append(ch[c]);
        }
        return buf.toString();
    }

    static String slurp(File f)
            throws IOException
    {
        int len = (int) f.length();
        byte[] buf = new byte[len];
        FileInputStream in = new FileInputStream(f);
        String contents = null;
        try {
            in.read(buf, 0, len);
            contents = new String(buf, "UTF-8");
        }
        finally {
            in.close();
        }
        return contents;
    }

    static private Environment env;
    private static String host;

    public static String getHost()
    {
        return host;
    }

    static {
        try {
            env = new Environment();
            host = env.getHost();
        }
        catch (IOException io) {
            io.printStackTrace();
        }
    }

    static Environment env()
    {
        if (env != null) {
            return env;
        }
        try {
            env = new Environment();
        }
        catch (IOException io) {
            io.printStackTrace();
        }
        return env;
    }
}
