package restx.exceptions;

import restx.common.UUIDGenerator;
import restx.common.UUIDGenerators;
import restx.factory.*;

import javax.inject.Named;
import java.util.List;

/**
 * @author fcamblor
 */
@Module
public class ExceptionsFactory {
    public static final String EXCEPTION_UUID_GENERATOR = "ExceptionUUIDGenerator";
    private static final Name<UUIDGenerator> EXCEPTION_UUID_GENERATOR_NAME = Name.of(UUIDGenerator.class, EXCEPTION_UUID_GENERATOR);

    @Provides @Named(EXCEPTION_UUID_GENERATOR)
    public UUIDGenerator exceptionUUIDGenerator(){
        return UUIDGenerator.DEFAULT;
    }

    public static UUIDGenerator currentUUIDGenerator(){
        return UUIDGenerators.currentGeneratorFor(EXCEPTION_UUID_GENERATOR_NAME);
    }

    public static void playbackUUIDs(List<String> sequence, Runnable runnable) {
        UUIDGenerators.playback(sequence, runnable, EXCEPTION_UUID_GENERATOR_NAME);
    }

    public static void recordUUIDs(Runnable runnable) {
        UUIDGenerators.record(runnable, EXCEPTION_UUID_GENERATOR_NAME);
    }

}
