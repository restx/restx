package restx.servlet;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.*;
import restx.factory.Component;
import restx.security.RestxPrincipal;
import restx.security.RestxSession;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;

import static restx.servlet.ServletModule.SERVLET_PRINCIPAL_CONVERTER;

/**
 * Date: 17/12/13
 * Time: 23:01
 */
@Component(priority = -180)
public class ServletSecurityFilter implements RestxFilter, RestxHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServletSecurityFilter.class);

    private final ServletPrincipalConverter servletPrincipalConverter;

    public ServletSecurityFilter(
            @Named(SERVLET_PRINCIPAL_CONVERTER) ServletPrincipalConverter servletPrincipalConverter) {
        this.servletPrincipalConverter = servletPrincipalConverter;
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        try {
            HttpServletRequest httpServletRequest = req.unwrap(HttpServletRequest.class);
            if (httpServletRequest.getUserPrincipal() != null) {
                return Optional.of(new RestxHandlerMatch(
                        new StdRestxRequestMatch("*", req.getRestxPath()),
                        this));
            } else {
                return Optional.absent();
            }
        } catch (IllegalArgumentException ex) {
            return Optional.absent();
        } catch (NoClassDefFoundError e) {
            if ("javax/servlet/http/HttpServletRequest".equals(e.getMessage())) {
                // this may happen when app depends on restx-servlet to have servlet support, but is run outside a servlet container
                // eg with restx-simple-server. In this case the provided dependency on servlet API (that is provided by
                // the servlet container when run inside such container) will make us fail with a NoClassDefFoundError
                return Optional.absent();
            } else {
                throw e;
            }
        }
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        HttpServletRequest httpServletRequest = req.unwrap(HttpServletRequest.class);
        final Principal userPrincipal = httpServletRequest.getUserPrincipal();
        if (userPrincipal != null) {
            logger.debug("setting restx principal from http servlet request {}", userPrincipal);
            if (userPrincipal instanceof RestxPrincipal) {
                RestxSession.current().authenticateAs((RestxPrincipal) userPrincipal);
            } else {
                RestxSession.current().authenticateAs(servletPrincipalConverter.toRestxPrincipal(userPrincipal));
            }
        }

        ctx.nextHandlerMatch().handle(req, resp, ctx);
    }

}
