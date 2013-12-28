package samplest.cors;

import com.google.common.base.Predicates;
import restx.factory.Module;
import restx.factory.Provides;
import restx.security.CORSAuthorizer;
import restx.security.StdCORSAuthorizer;

import static java.util.Arrays.asList;

/**
 * Date: 28/12/13
 * Time: 10:09
 */
@Module
public class CORSSamplestModule {
    @Provides
    public CORSAuthorizer samplestAuthorizer1() {
        return new StdCORSAuthorizer(
                Predicates.<CharSequence>equalTo("http://localhost:9000"),
                Predicates.containsPattern("^/cors/1"), asList("GET", "POST"));
    }
    @Provides
    public CORSAuthorizer samplestAuthorizer2() {
        return new StdCORSAuthorizer(
                Predicates.<CharSequence>alwaysTrue(),
                Predicates.containsPattern("^/cors/2"), asList("GET", "HEAD", "POST"));
    }
    @Provides
    public CORSAuthorizer samplestAuthorizer3() {
        return new StdCORSAuthorizer(
                Predicates.<CharSequence>alwaysTrue(),
                Predicates.containsPattern("^/cors/3"), asList("PUT"));
    }
}
