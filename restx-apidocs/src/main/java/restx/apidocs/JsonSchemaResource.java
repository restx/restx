package restx.apidocs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import restx.WebException;
import restx.admin.AdminModule;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.jackson.FrontObjectMapperFactory;
import restx.security.RolesAllowed;

import javax.inject.Named;

/**
 * Date: 8/12/13
 * Time: 21:00
 */
@RestxResource(group = "restx-admin") @Component
public class JsonSchemaResource {
    private final ObjectMapper mapper;

    public JsonSchemaResource(@Named(FrontObjectMapperFactory.MAPPER_NAME) ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @GET("/@/api-docs/schemas/{fqcn}")
    @RolesAllowed(AdminModule.RESTX_ADMIN_ROLE)
    public String getJsonSchema(String fqcn) {
        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        try {
            mapper.acceptJsonFormatVisitor(mapper.constructType(Class.forName(fqcn)), visitor);
        } catch (JsonMappingException e) {
            throw new IllegalStateException(e);
        } catch (ClassNotFoundException e) {
            throw new WebException(HttpStatus.NOT_FOUND);
        }
        try {
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(visitor.finalSchema());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
