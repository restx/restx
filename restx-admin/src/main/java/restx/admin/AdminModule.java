package restx.admin;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;

import java.io.IOException;
import java.util.regex.Pattern;
import restx.RestxContext;
import restx.RestxFilter;
import restx.RestxHandler;
import restx.RestxHandlerMatch;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxResponse;
import restx.StdRestxRequestMatch;
import restx.WebException;
import restx.factory.Module;
import restx.factory.Provides;
import restx.http.HttpStatus;
import restx.security.*;

import javax.inject.Named;

@Module(priority = 10000)
public class AdminModule {
    public static final String RESTX_ADMIN_ROLE = "restx-admin";

    public static final RestxAdminPrincipal RESTX_ADMIN_PRINCIPAL = new RestxAdminPrincipal();

    @Provides
    @Named("restx.admin.password")
    public String restxAdminPassword() {
        return "juma";
    }

    @Provides
    @Named("restx.admin.passwordHash")
    public String restxAdminPasswordHash(@Named("restx.admin.password") String password) {
        return Hashing.md5().hashString(password, Charsets.UTF_8).toString();
    }

    @Provides
    public BasicPrincipalAuthenticator basicPrincipalAuthenticator(
            @Named("restx.admin.passwordHash") final String adminPasswordHash, SecuritySettings securitySettings) {
        return new StdBasicPrincipalAuthenticator(new UserService<RestxAdminPrincipal>() {
            @Override
            public Optional<RestxAdminPrincipal> findUserByName(String name) {
                return "admin".equals(name) ? Optional.of(RESTX_ADMIN_PRINCIPAL) : Optional.<RestxAdminPrincipal>absent();
            }

            @Override
            public Optional<RestxAdminPrincipal> findAndCheckCredentials(String name, String passwordHash) {
                return "admin".equals(name) && adminPasswordHash.equals(passwordHash) ?
                        Optional.of(RESTX_ADMIN_PRINCIPAL) : Optional.<RestxAdminPrincipal>absent();
            }
        }, securitySettings);
    }

    public static class RestxAdminPrincipal implements RestxPrincipal {
        @Override
        public ImmutableSet<String> getPrincipalRoles() {
            return ImmutableSet.of(RESTX_ADMIN_ROLE);
        }

        @Override
        public String getName() {
            return "admin";
        }
    }

    @Provides
    public RestxFilter adminRoleFilter() {
        return new RestxFilter() {
            final Pattern privatePath = Pattern.compile("^/@/(?!(ui|webjars)/).*$");

            @Override
            public Optional<RestxHandlerMatch> match(RestxRequest req) {
                if (privatePath.matcher(req.getRestxPath()).find()) {
                    return Optional.of(new RestxHandlerMatch(
                            new StdRestxRequestMatch("/@/*", req.getRestxPath()),
                            new RestxHandler() {
                                @Override
                                public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                                    final RestxSession current = RestxSession.current();
                                    if (current.getPrincipal().isPresent() &&
                                            Permissions.hasRole(RESTX_ADMIN_ROLE).has(current.getPrincipal().get(), req, match).isPresent()) {
                                        ctx.nextHandlerMatch().handle(req, resp, ctx);
                                    } else {
                                        throw new WebException(HttpStatus.UNAUTHORIZED);
                                    }
                                }
                            }
                    ));
                }
                return Optional.absent();
            }
        };
    }
}
