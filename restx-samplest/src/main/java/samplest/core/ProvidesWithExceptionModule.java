package samplest.core;

import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownServiceException;

/**
 * Date: 16/12/13
 * Time: 18:36
 */
@Module
public class ProvidesWithExceptionModule {
    @Provides @Named("providesWithExceptions")
    public String mayThrowException() throws UnknownServiceException, MalformedURLException {
        return "noproblem";
    }
}
