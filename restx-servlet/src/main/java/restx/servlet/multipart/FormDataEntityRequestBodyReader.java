package restx.servlet.multipart;

import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.RestxContext;
import restx.RestxRequest;
import restx.WebException;
import restx.entity.EntityRequestBodyReader;
import restx.http.HttpStatus;
import restx.servlet.HttpServletRestxRequest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;

public class FormDataEntityRequestBodyReader implements EntityRequestBodyReader<Collection<Part>> {

    private static final Logger logger = LoggerFactory.getLogger(FormDataEntityRequestBodyReader.class);

    @Override
    public Type getType() {
        return MultipartFormDataContentTypeModule.TYPE;
    }

    //TODO Declare Multipart annotation to wrap @Consumes("multipart/form-data") and @MultipartConfig
    // to create MultipartConfigElement
    @Override
    public Collection<Part> readBody(RestxRequest req, RestxContext ctx) throws IOException {
        try {
            return ((HttpServletRestxRequest) req).getParts();
        } catch (Exception e) {
            logger.error("Can't parse request", e);
            throw new WebException(HttpStatus.BAD_REQUEST, String.format("Can't parse request\n%s", e));
        }
    }
}
