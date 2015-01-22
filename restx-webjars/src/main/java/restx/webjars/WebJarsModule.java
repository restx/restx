package restx.webjars;

import restx.ResourcesRoute;
import restx.factory.Module;
import restx.factory.Provides;

/**
 * @author fcamblor
 */
@Module
public class WebJarsModule {

    @Provides
    public ResourcesRoute webjars() {
        return new ResourcesRoute("WebjarsRoute", "/@/webjars", "META-INF/resources/webjars");
    }
}
