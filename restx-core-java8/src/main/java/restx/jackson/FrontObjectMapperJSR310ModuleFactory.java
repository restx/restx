package restx.jackson;

import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import restx.factory.Module;
import restx.factory.Provides;

@Module
public class FrontObjectMapperJSR310ModuleFactory {

    @Provides
    public com.fasterxml.jackson.databind.Module jsr310Module() {
        return new JSR310Module();
    }

}
