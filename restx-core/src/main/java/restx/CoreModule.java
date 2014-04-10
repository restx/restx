package restx;

import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.common.UUIDGenerator;
import restx.common.metrics.api.health.HealthCheckRegistry;
import restx.common.metrics.dummy.health.DummyHealthCheckRegistry;
import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.AutoStartable;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 */
@Module(priority = 10000)
public class CoreModule {
    private static final Logger logger = LoggerFactory.getLogger(CoreModule.class);
    public static final String UUID_GENERATOR = "UUIDGenerator";

    @Provides
    public ConfigSupplier coreAppConfigSupplier(ConfigLoader configLoader) {
        return configLoader.fromResource("restx/appConfig");
    }

    @Provides
    public ConfigSupplier httpConfigSupplier(ConfigLoader configLoader) {
        return configLoader.fromResource("restx/httpConfig");
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
        Class<HealthCheckRegistry> healthCheckRegistryClass = null;
        try {
            healthCheckRegistryClass = (Class<HealthCheckRegistry>) Class.forName("restx.metrics.codahale.health.CodahaleHealthCheckRegistry");
        } catch (ClassNotFoundException e){
            return new DummyHealthCheckRegistry();
        }

        try {
            HealthCheckRegistry healthCheckRegistry = healthCheckRegistryClass.newInstance();
            return healthCheckRegistry;
        } catch (Exception e) {
            throw new RuntimeException("Unable to instanciate class" + healthCheckRegistryClass, e);
        }
    }

    @Provides @Named(UUID_GENERATOR)
    public UUIDGenerator uuidGenerator() {
        return new UUIDGenerator.DefaultUUIDGenerator();
    }
}
