package restx.webjars;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import restx.ResourcesRoute;
import restx.factory.Module;
import restx.factory.Provides;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author fcamblor
 */
@Module
public class WebJarsModule {

    @Provides
    public ResourcesRoute webjars() {
        return new ResourcesRoute(
                "WebjarsRoute", "/@/webjars", "META-INF/resources/webjars",
                ImmutableMap.<String, String>of(),
                Arrays.asList(
                        new ResourcesRoute.CachedResourcePolicy(
                                Predicates.<ResourcesRoute.ResourceInfo>alwaysTrue(),
                                "max-age="+ TimeUnit.DAYS.toSeconds(100)
                        )
                )
        );
    }
}
