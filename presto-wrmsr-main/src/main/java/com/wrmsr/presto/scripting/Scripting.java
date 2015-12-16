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
package com.wrmsr.presto.scripting;

import org.apache.commons.lang.NotImplementedException;

import javax.script.ScriptEngine;

public class Scripting
{
    private final String name;
    private final ScriptEngine scriptEngine;

    public Scripting(String name, ScriptEngine scriptEngine)
    {
        this.name = name;
        this.scriptEngine = scriptEngine;
    }

    // http://www.drdobbs.com/jvm/jsr-223-scripting-for-the-java-platform/215801163?pgno=2
    public Object invokeFunction(String name, Object... args)
    {
        throw new NotImplementedException();
    }
}
