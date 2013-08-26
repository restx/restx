package restx.security;

import com.google.common.cache.CacheLoader;
import restx.common.UUIDGenerator;
import restx.common.UUIDGenerators;
import restx.factory.Module;
import restx.factory.Name;
import restx.factory.Provides;

import javax.inject.Named;
import java.util.List;

/**
 */
@Module(priority = 10000)
public class BasicSecurityModule {
    public static final String SESSION_UUID_GENERATOR = "SessionUUIDGenerator";
    private static final Name<UUIDGenerator> SESSION_UUID_GENERATOR_NAME = Name.of(UUIDGenerator.class, SESSION_UUID_GENERATOR);

    @Provides
    @Named(RestxPrincipal.SESSION_DEF_KEY)
    public RestxSession.Definition.Entry principalSessionEntry(final BasicPrincipalAuthenticator authenticator) {
        return new RestxSession.Definition.Entry(RestxPrincipal.class, RestxPrincipal.SESSION_DEF_KEY,
                new CacheLoader<String, RestxPrincipal>() {
            @Override
            public RestxPrincipal load(String key) throws Exception {
                return authenticator.findByName(key).orNull();
            }
        });
    }

    @Provides
    public RestxSession.Definition.Entry sessionKeySessionEntry() {
        return new RestxSession.Definition.Entry(String.class, Session.SESSION_DEF_KEY,
                new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return key;
            }
        });
    }

    @Provides @Named(SESSION_UUID_GENERATOR)
    public UUIDGenerator sessionUUIDGenerator(){
        return UUIDGenerator.DEFAULT;
    }

    public static UUIDGenerator currentUUIDGenerator(){
        return UUIDGenerators.currentGeneratorFor(SESSION_UUID_GENERATOR_NAME);
    }

    public static void playbackUUIDs(List<String> sequence, Runnable runnable) {
        UUIDGenerators.playback(sequence, runnable, SESSION_UUID_GENERATOR_NAME);
    }

    public static void recordUUIDs(Runnable runnable) {
        UUIDGenerators.record(runnable, SESSION_UUID_GENERATOR_NAME);
    }
}
