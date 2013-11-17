package restx;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.AutoStartable;
import restx.factory.Module;
import restx.factory.Provides;

/**
 */
@Module(priority = 10000)
public class CoreModule {
    private static final Logger logger = LoggerFactory.getLogger(CoreModule.class);

    @Provides
    public ConfigSupplier coreAppConfigSupplier(ConfigLoader configLoader) {
        return configLoader.fromResource("restx/appConfig");
    }

    @Provides
    public EventBus eventBus() {
        return new EventBus();
    }

    @Provides
    public AutoStartable loadEventBusOnStartUp(EventBus eventBus) {
        return new AutoStartable() {
            @Override
            public void start() {
                logger.debug("started EventBus");
            }
        };
    }

    @Provides
    public HealthCheckRegistry healthCheckRegistry() {
        return new HealthCheckRegistry();
    }
}
