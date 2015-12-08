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
package com.wrmsr.presto.function;

import com.facebook.presto.metadata.FunctionResolver;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.wrmsr.presto.function.bitwise.BitwiseModule;

public class FunctionModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        binder.install(new BitwiseModule());

        Multibinder<FunctionRegistration> functionRegistrationBinder = Multibinder.newSetBinder(binder, FunctionRegistration.class);
        Multibinder<FunctionResolverRegistration> functionResolverRegistrationBinder = Multibinder.newSetBinder(binder, FunctionResolverRegistration.class);

        binder.bind(JdbcFunction.class).asEagerSingleton();
        functionRegistrationBinder.addBinding().to(JdbcFunction.class);

        functionRegistrationBinder.addBinding().toInstance(FunctionRegistration.of(flb -> {
            flb
                    .scalar(CompressionFunctions.class)
                    .scalar(GrokFunctions.class);
        }));
    }
}
