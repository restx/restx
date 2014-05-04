package restx.servlet;

import com.google.common.collect.ImmutableSet;
import restx.common.Version;
import restx.factory.Module;
import restx.factory.Provides;
import restx.security.RestxPrincipal;

import javax.inject.Named;
import java.security.Principal;

/**
 * Date: 23/4/14
 * Time: 20:58
 */
@Module(priority = 200)
public class ServletModule {
    public static final String SERVLET_PRINCIPAL_CONVERTER = "ServletPrincipalConverter";

    @Provides @Named(SERVLET_PRINCIPAL_CONVERTER)
    public ServletPrincipalConverter defaultServletPrincipalConverter() {
        return new ServletPrincipalConverter() {
            @Override
            public RestxPrincipal toRestxPrincipal(final Principal principal) {
                return new RestxPrincipal() {
                    @Override
                    public ImmutableSet<String> getPrincipalRoles() {
                        return ImmutableSet.of();
                    }

                    @Override
                    public String getName() {
                        return principal.getName();
                    }
                };
            }
        };
    }

    @Provides
    public RegisteredServerType jettyServerType() {
        return new RegisteredServerType(
                "Jetty " + Version.getVersion("org.eclipse.jetty", "jetty-server"), "org.eclipse.jetty");
    }

    @Provides
    public RegisteredServerType tomcatServerType() {
        return new RegisteredServerType(
                "Apache Tomcat " + Version.getVersion("org.apache.tomcat", "tomcat-catalina"), "org.eclipse.jetty");
    }
}
