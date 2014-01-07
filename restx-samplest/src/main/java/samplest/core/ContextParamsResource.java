package samplest.core;

import restx.RestxRequest;
import restx.annotations.GET;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import java.util.List;
import java.util.Locale;

/**
 * Date: 5/12/13
 * Time: 07:09
 */
@RestxResource @Component
public class ContextParamsResource {
    @PermitAll
    @GET("/contextParams/baseUri")
    public String getBaseUri(@Param(kind = Param.Kind.CONTEXT, value = "baseUri") String baseUri) {
        return baseUri;
    }
    @PermitAll
    @GET("/contextParams/clientAddress")
    public String getClientAddress(@Param(kind = Param.Kind.CONTEXT, value = "clientAddress") String clientAddress) {
        return clientAddress;
    }
    @PermitAll
    @GET("/contextParams/request")
    public String getRequest(@Param(kind = Param.Kind.CONTEXT, value = "request") RestxRequest request) {
        return request.toString();
    }
    @PermitAll
    @GET("/contextParams/locale")
    public String getLocale(@Param(kind = Param.Kind.CONTEXT, value = "locale") Locale locale) {
        return locale.toLanguageTag();
    }
    @PermitAll
    @GET("/contextParams/locales")
    public String getLocales(@Param(kind = Param.Kind.CONTEXT, value = "locales") List<Locale> locales) {
        return locales.toString();
    }
}
