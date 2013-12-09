package restx;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.common.UUIDGenerator;
import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.*;

import javax.inject.Named;
import java.util.Iterator;
import java.util.List;
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
        return new UUIDGenerator() {
            @Override
            public String doGenerate() {
                return UUID.randomUUID().toString();
            }
        };
    }

    /**
     * Exceutes a runnable while playingback a list of UUIDs when code is using current factory to get a UUIDGenerator.
     *
     * This should be used for testing only.
     *
     * @param uuids
     * @param runnable
     */
    public static void playbackUUIDs(final List<String> uuids, Runnable runnable) {
        overrideComponents()
                .set(UUIDGenerator.class, CoreModule.UUID_GENERATOR, new PlaybackUUIDGenerator(uuids));
        Factory.setCurrent(Factory.newInstance());

        try {
            runnable.run();
        } finally {
            Factory.clearCurrent();
            overrideComponents().clear();
        }
    }

    private static class PlaybackUUIDGenerator implements UUIDGenerator {
        private Iterator<String> uuidsSequence;

        public PlaybackUUIDGenerator(List<String> uuids) {
            uuidsSequence = uuids.iterator();
        }

        @Override
        public String doGenerate() {
            return uuidsSequence.next();
        }
    }
}
