package restx.swagger;

import restx.RestxRoute;
import restx.factory.DefaultFactoryMachine;
import restx.factory.Factory;
import restx.factory.Name;
import restx.jackson.FrontObjectMapperFactory;
import restx.security.CORSAuthorizer;

/**
 * User: xavierhanin
 * Date: 2/7/13
 * Time: 9:13 AM
 */
public class SwaggerMachine extends DefaultFactoryMachine {
    private static final SwaggerProvider SWAGGER_PROVIDER = new SwaggerProvider();

    public SwaggerMachine() {
        super(0,
                SingleComponentBoxSupplier.boundless(Name.of(RestxRoute.class, "SwaggerIndexRoute"),
                        new SingleComponentSupplier<RestxRoute>() {
                            @Override
                            public RestxRoute newComponent(Factory factory) {
                                return new SwaggerIndexRoute("SwaggerIndexRoute",
                                        factory.mustGetNamedComponent(FrontObjectMapperFactory.NAME).getComponent(),
                                        factory);
                            }
                        })
                ,
                SingleComponentBoxSupplier.boundless(Name.of(RestxRoute.class, "SwaggerApiDeclarationRoute"),
                        new SingleComponentSupplier<RestxRoute>() {
                            @Override
                            public RestxRoute newComponent(Factory factory) {
                                return new SwaggerApiDeclarationRoute("SwaggerApiDeclarationRoute",
                                        factory.mustGetNamedComponent(FrontObjectMapperFactory.NAME).getComponent(),
                                        factory);
                            }
                        })
                ,
                SingleComponentBoxSupplier.boundless(Name.of(CORSAuthorizer.class, "SwaggerCORSAuthorizer"),
                        new SingleComponentSupplier<CORSAuthorizer>() {
                            @Override
                            public CORSAuthorizer newComponent(Factory factory) {
                                return SWAGGER_PROVIDER.getSwaggerAuthorizer();
                            }
                        })
        );
    }
}
