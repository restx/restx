package restx.entity.text;

import com.google.common.base.Optional;
import restx.RestxContext;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.entity.AbstractEntityResponseWriter;
import restx.entity.EntityResponseWriter;
import restx.entity.EntityResponseWriterFactory;
import restx.factory.Component;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Locale;

/**
 * Date: 23/10/13
 * Time: 11:36
 */
@Component
public class TextEntityResponseWriterFactory implements EntityResponseWriterFactory {
    @Override
    public <T> Optional<? extends EntityResponseWriter<T>> mayBuildFor(Type valueType, String contentType) {
        if (!contentType.toLowerCase(Locale.ENGLISH).startsWith("text/plain")) {
            return Optional.absent();
        }

        return Optional.of(new AbstractEntityResponseWriter<T>("text/plain") {
            @Override
            protected void write(T value, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
                resp.getWriter().append(value.toString());
            }
        });
    }
}
