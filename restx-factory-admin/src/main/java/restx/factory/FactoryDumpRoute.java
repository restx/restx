package restx.factory;

import restx.*;
import restx.admin.AdminModule;
import restx.security.PermissionFactory;
import restx.security.RestxSecurityManager;

import javax.inject.Inject;
import java.io.IOException;

@Component
public class FactoryDumpRoute extends StdRoute {
    private final Factory factory;
    private final RestxSecurityManager securityManager;
    private PermissionFactory permissionFactory;

    @Inject
    public FactoryDumpRoute(Factory factory, RestxSecurityManager securityManager, final PermissionFactory permissionFactory) {
        super("FactoryRoute", new StdRestxRequestMatcher("GET", "/@/factory"));
        this.factory = factory;
        this.securityManager = securityManager;
        this.permissionFactory = permissionFactory;
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        securityManager.check(req, match, permissionFactory.hasRole(AdminModule.RESTX_ADMIN_ROLE));
        resp.setContentType("text/plain");
        resp.getWriter().println(factory.dump());
    }
}
