package {package};

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import dagger.Module;
import dagger.Provides;
import java.io.IOException;
import restx.*;

import javax.inject.Named;

@Module(
        entryPoints = {router}.class,
        includes = {
{includeModules}
        })
public class {router}Module extends RestxRouterModule {
    public Class<? extends RestxRoute> router() {
        return {router}.class;
    }

{provideRoutes}

    public String toString() {
        return "{router}Module[{countRoutes} routes]";
    }
}
