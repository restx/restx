package restx.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import restx.RestxRoute;
import restx.factory.*;
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
                new StdMachineEngine<RestxRoute>(Name.of(RestxRoute.class, "SwaggerIndexRoute"), BoundlessComponentBox.FACTORY) {
                    private final Factory.Query<ObjectMapper> mapper = Factory.Query.byName(FrontObjectMapperFactory.NAME);
                    private final Factory.Query<Factory> factory = Factory.Query.factoryQuery();
                    @Override
                    public RestxRoute doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return new SwaggerIndexRoute("SwaggerIndexRoute",
                                satisfiedBOM.getOne(mapper).get().getComponent(),
                                satisfiedBOM.getOne(factory).get().getComponent());
                    }

                    @Override
                    public BillOfMaterials getBillOfMaterial() {
                        return BillOfMaterials.of(mapper, factory);
                    }
                }
                ,
                new StdMachineEngine<RestxRoute>(Name.of(RestxRoute.class, "SwaggerApiDeclarationRoute"), BoundlessComponentBox.FACTORY) {
                    private final Factory.Query<ObjectMapper> mapper = Factory.Query.byName(FrontObjectMapperFactory.NAME);
                    private final Factory.Query<Factory> factory = Factory.Query.factoryQuery();
                    @Override
                    public RestxRoute doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return new SwaggerApiDeclarationRoute("SwaggerApiDeclarationRoute",
                                satisfiedBOM.getOne(mapper).get().getComponent(),
                                satisfiedBOM.getOne(factory).get().getComponent());
                    }

                    @Override
                    public BillOfMaterials getBillOfMaterial() {
                        return BillOfMaterials.of(mapper, factory);
                    }
                }
                ,
                new NoDepsMachineEngine<RestxRoute>(Name.of(RestxRoute.class, "SwaggerUIRoute"), BoundlessComponentBox.FACTORY) {
                    @Override
                    protected RestxRoute doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return new SwaggerUIRoute();
                    }
                }
                ,
                new NoDepsMachineEngine<CORSAuthorizer>(Name.of(CORSAuthorizer.class, "SwaggerCORSAuthorizer"),
                        BoundlessComponentBox.FACTORY) {
                    @Override
                    public CORSAuthorizer doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return SWAGGER_PROVIDER.getSwaggerAuthorizer();
                    }
                }
        );
    }
}
