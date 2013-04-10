package restx.apidocs;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecRepository;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * User: xavierhanin
 * Date: 4/2/13
 * Time: 9:09 PM
 */
@Component @RestxResource(group = "admin")
public class SpecsResource {
    private final RestxSpecRepository repository;

    public SpecsResource(RestxSpecRepository repository) {
        this.repository = repository;
    }

    @GET("/@/specs")
    public Iterable<String> findSpecsForOperation(String httpMethod, String path) {
        return repository.findSpecsByOperation(httpMethod, path);
    }

    @GET("/@/specs/{id}")
    public Optional<RestxSpec> getSpecById(String id) {
        try {
            return repository.findSpecById(URLDecoder.decode(id, Charsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
