package com.wrmsr.presto.scripting;

import com.facebook.presto.metadata.SqlFunction;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.wrmsr.presto.function.FunctionRegistration;
import com.wrmsr.presto.spi.ScriptEngineProvider;

public class ScriptingModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        Multibinder<FunctionRegistration> functionRegistrationBinder = Multibinder.newSetBinder(binder, FunctionRegistration.class);
        MapBinder<String, ScriptEngineProvider> scriptEngineProviderBinder = MapBinder.newMapBinder(binder, String.class, ScriptEngineProvider.class);

        binder.bind(ScriptingManager.class).asEagerSingleton();

        binder.bind(ScriptFunction.Factory.class).toProvider(FactoryProvider.newFactory(ScriptFunction.Factory.class, ScriptFunction.class));
        binder.bind(ScriptFunction.Registration.MaxArity.class).toInstance(new ScriptFunction.Registration.MaxArity(3));
        binder.bind(ScriptFunction.Registration.class).asEagerSingleton();
        functionRegistrationBinder.addBinding().to(ScriptFunction.Registration.class);
    }
}
