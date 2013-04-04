package restx.apidocs;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import restx.factory.Module;
import restx.factory.Provides;
import restx.security.CORSAuthorizer;
import restx.security.StdCORSAuthorizer;

import java.util.regex.Pattern;

/**
 * User: xavierhanin
 * Date: 2/8/13
 * Time: 1:50 PM
 */
@Module
public class ApiDocsModule {
    @Provides
    public CORSAuthorizer getSwaggerAuthorizer() {
        return new StdCORSAuthorizer(
                Predicates.<CharSequence>alwaysTrue(),
                Predicates.contains(Pattern.compile("^/@/api-docs")),
                ImmutableList.of("GET"));
    }
}
