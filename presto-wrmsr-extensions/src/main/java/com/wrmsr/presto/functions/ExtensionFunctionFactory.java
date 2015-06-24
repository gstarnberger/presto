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
package com.wrmsr.presto.functions;

import com.facebook.presto.metadata.FunctionFactory;
import com.facebook.presto.metadata.FunctionListBuilder;
import com.facebook.presto.metadata.FunctionRegistry;
import com.facebook.presto.metadata.ParametricFunction;
import com.facebook.presto.spi.type.TypeManager;

import java.util.List;

public class ExtensionFunctionFactory
        implements com.facebook.presto.metadata.FunctionFactory
{
    private final TypeManager typeManager;
    private final FunctionRegistry functionRegistry;
    private final TypeRegistrar typeRegistrar;

    public ExtensionFunctionFactory(TypeManager typeManager, FunctionRegistry functionRegistry, TypeRegistrar typeRegistrar)
    {
        this.typeManager = typeManager;
        this.functionRegistry = functionRegistry;
        this.typeRegistrar = typeRegistrar;
    }

    @Override
    public List<ParametricFunction> listFunctions()
    {
        return new FunctionListBuilder(typeManager)
                .scalar(CompressionFunctions.class)
                .function(new SerializeFunction(functionRegistry, typeRegistrar))
                .function(Hash.HASH)
                .getFunctions();
    }
}

