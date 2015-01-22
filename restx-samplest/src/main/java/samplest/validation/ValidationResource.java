package samplest.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.annotations.POST;
import restx.annotations.PUT;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;
import restx.validation.ValidatedFor;
import restx.validation.stereotypes.FormValidations;

/**
 * @author fcamblor
 */
@RestxResource @Component
public class ValidationResource {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationResource.class);

    public static interface MyCustomValidationGroup{}

    @PermitAll
    @POST("/valid/pojos")
    public void createPOJOWithoutAnnotation(POJO myPojo) {
        LOG.info("Pojo {} {} created !", myPojo.getName(), myPojo.getSubPOJO().getLabel());
    }

    @PermitAll
    @POST("/valid/pojos2")
    public void createPOJOWithAnnotation(@ValidatedFor(FormValidations.Create.class) POJO myPojo) {
        LOG.info("Pojo {} {} created !", myPojo.getName(), myPojo.getSubPOJO().getLabel());
    }

    @PermitAll
    @PUT("/valid/pojos/{id}")
    public void createPOJOWithoutAnnotation(Long id, @ValidatedFor({MyCustomValidationGroup.class, FormValidations.Update.class}) POJO myPojo) {
        LOG.info("Pojo {} {} updated !", myPojo.getName(), myPojo.getSubPOJO().getLabel());
    }
}
