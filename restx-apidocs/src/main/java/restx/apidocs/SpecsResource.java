package restx.apidocs;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import restx.annotations.GET;
import restx.annotations.PUT;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.specs.RestxSpec;
import restx.specs.RestxSpecRepository;
import restx.specs.ThenHttpResponse;
import restx.specs.When;

import java.io.IOException;
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

    @PUT("/@/specs/{id}/wts/{wtsIndex}/then")
    public Optional<ThenHttpResponse> updateSpecThenHttp(String id, int wtsIndex, ThenHttpResponse response) throws IOException {
        try {
            Optional<RestxSpec> spec = repository.findSpecById(URLDecoder.decode(id, Charsets.UTF_8.name()));

            if (!spec.isPresent()) {
                return Optional.absent();
            }

            if (wtsIndex >= spec.get().getWhens().size()) {
                return Optional.absent();
            }

            When when = spec.get().getWhens().get(wtsIndex);
            spec.get().withWhenAt(wtsIndex, when.withThen(response)).store();

            return Optional.of(response);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
