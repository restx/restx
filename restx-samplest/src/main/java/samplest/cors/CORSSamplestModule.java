package samplest.cors;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import restx.factory.Module;
import restx.factory.Provides;
import restx.security.CORSAuthorizer;
import restx.security.StdCORSAuthorizer;

/**
 * Date: 28/12/13
 * Time: 10:09
 */
@Module
public class CORSSamplestModule {
    @Provides
    public CORSAuthorizer samplestAuthorizer1() {
        return StdCORSAuthorizer.builder().setOriginMatcher(
                Predicates.<CharSequence>equalTo("http://localhost:9000"))
                .setPathMatcher(Predicates.containsPattern("^/cors/1"))
                .setAllowedMethods(ImmutableList.of("GET", "POST"))
                .build();
    }
    @Provides
    public CORSAuthorizer samplestAuthorizer2() {
        return StdCORSAuthorizer.builder()
                .setOriginMatcher(Predicates.<CharSequence>alwaysTrue())
                .setPathMatcher(Predicates.containsPattern("^/cors/2"))
                .setAllowedMethods(ImmutableList.of("GET", "HEAD", "POST"))
                .build();
    }
    @Provides
    public CORSAuthorizer samplestAuthorizer3() {
        return StdCORSAuthorizer.builder()
                .setOriginMatcher(Predicates.<CharSequence>alwaysTrue())
                .setPathMatcher(Predicates.containsPattern("^/cors/3"))
                .setAllowedMethods(ImmutableList.of("PUT"))
                .build();
    }
}
