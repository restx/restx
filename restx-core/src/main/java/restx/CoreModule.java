package restx;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.common.UUIDGenerator;
import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.*;
import restx.i18n.DefaultMessages;
import restx.i18n.DefaultMutableMessages;
import restx.i18n.Messages;
import restx.i18n.SupportedLocale;

import javax.inject.Named;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static restx.factory.Factory.LocalMachines.overrideComponents;

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

    @Provides @Named("Messages")
    public Messages messages(AppSettings appSettings) {
        if (RestxContext.Modes.PROD.equals(appSettings.mode())) {
            return new DefaultMessages("labels", StandardCharsets.UTF_8);
        } else {
            return new DefaultMutableMessages("labels", StandardCharsets.UTF_8);
        }
    }

    @Provides @Named("ROOT")
    public SupportedLocale rootSupportedLocale() {
        return new SupportedLocale(Locale.ROOT);
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

    @Provides @Named(UUID_GENERATOR)
    public UUIDGenerator uuidGenerator() {
        return new UUIDGenerator.DefaultUUIDGenerator();
    }
}
