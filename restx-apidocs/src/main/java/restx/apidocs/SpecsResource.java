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
@Component @RestxResource(group = "restx-admin")
public class SpecsResource {
    private final RestxSpecRepository repository;
    private final RestxSpec.Storage storage;

    public SpecsResource(RestxSpecRepository repository, RestxSpec.StorageSettings storageSettings) {
        this.repository = repository;
        storage = RestxSpec.Storage.with(storageSettings);
    }

    @GET("/@/specs")
    public Iterable<String> findSpecsForOperation(String httpMethod, String path) {
        return repository.findSpecsByOperation(httpMethod, path);
    }

    @GET("/@/specs/{id}")
    public Optional<RestxSpec> getSpecById(String id) {
        try {
            // to decode the spec id we first URL decode it, and then we replace triple underscores by forward slashes
            // This let the client address the spec id without having to URI encode the forward slash as %2F, which is
            // not allowed on some servers (especially tomcat)
            // see https://github.com/restx/restx/issues/90
            String specId = URLDecoder.decode(id, Charsets.UTF_8.name()).replace("___", "/");
            return repository.findSpecById(specId);
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

            When<ThenHttpResponse> when = asWhenHttp(spec.get().getWhens().get(wtsIndex));
            storage.store(
                    spec.get().withWhenAt(wtsIndex, when.withThen(response)));

            return Optional.of(response);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private When<ThenHttpResponse> asWhenHttp(When<?> when) {
        return (When<ThenHttpResponse>) when;
    }

}
