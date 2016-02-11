package restx.security;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import restx.RestxContext;
import restx.RestxHandler;
import restx.RestxHandlerMatch;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxResponse;
import restx.RestxRoute;
import restx.StdRestxRequestMatch;
import restx.WebException;
import restx.common.RestxConfig;
import restx.config.SettingsKey;
import restx.factory.AutoStartable;
import restx.factory.Module;
import restx.factory.Provides;
import restx.factory.When;
import restx.http.HttpStatus;
import restx.security.RestxSession.Definition.EntryCacheManager;

import javax.inject.Named;

/**
 * Date: 17/11/13
 * Time: 17:55
 */
@Module(priority = 100)
public class SecurityModule {
    private static final Logger logger = LoggerFactory.getLogger(SecurityModule.class);
    public static final String ENTRY_CACHE_MANAGER = "EntryCacheManager";

    public static interface SecuritySettings {
        @SettingsKey(key = "restx.sessions.stats.limit", defaultValue = "100",
                doc = "The maximum number of sessions data to keep in memory for statistics in the monitor view")
        int sessionsLimit();

    }

    @Provides
    @Named("restx.activation::restx.security.RestxSessionBareFilter::RestxSessionBareFilter")
    public String disableBareFilter() {
        return "false";
    }

    @Provides @Named("Sessions")
    public Sessions session(SecuritySettings settings) {
        return new Sessions(settings.sessionsLimit());
    }

    // this makes Sessions part of the auto started components, thus making it server wide.
    @Provides
    public AutoStartable startSessions(@Named("Sessions") Sessions sessions) {
        return new AutoStartable() {
            @Override
            public void start() {
                logger.debug("starting sessions statistics");
            }
        };
    }

    // provides Settings instead of annotating the interface, otherwise it will
    // hide the other restx.security.SecuritySettingsConfig classes
    @Provides
    public SecuritySettings securitySettings(final RestxConfig config) {
        return new SecuritySettings() {
            @Override
            public int sessionsLimit() {
                return config.getInt("restx.sessions.stats.limit").or(100);
            }
        };
    }

    @Provides @Named(ENTRY_CACHE_MANAGER)
    public EntryCacheManager guavaCacheManager() {
        return new GuavaEntryCacheManager();
    }

    @Provides(priority = 100000)
    @When(name = "restx.mode", value = "prod")
    public RestxRoute productionNotFoundHandler() {
        return new RestxRoute() {
            @Override
            public Optional<RestxHandlerMatch> match(RestxRequest req) {
                return Optional.of(new RestxHandlerMatch(
                        new StdRestxRequestMatch("*", req.getRestxPath()),
                        new RestxHandler() {
                            @Override
                            public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                                throw new WebException(HttpStatus.NOT_FOUND);
                            }
                        }
                ));
            }
        };
    }
}
