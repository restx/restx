package restx.servlet.multipart;

import com.google.common.base.Optional;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.entity.EntityDefaultContentTypeProvider;
import restx.entity.EntityRequestBodyReaderFactory;
import restx.factory.Module;
import restx.factory.Provides;
import restx.types.TypeReference;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Locale;

@Module
public class MultipartFormDataContentTypeModule {

    private static final Logger logger = LoggerFactory.getLogger(MultipartFormDataContentTypeModule.class);

    public static final String FORM_DATA_CONTENT_TYPE = "multipart/form-data";
    public static final Type TYPE = new TypeReference<Collection<Part>>() {}.getType();

    @Provides
    public EntityDefaultContentTypeProvider formDataDefaultContentTypeProvider() {
        return new EntityDefaultContentTypeProvider() {
            @Override
            public Optional<String> mayProvideDefaultContentType(Type type) {
                if (TYPE.equals(type)) {
                    return Optional.of(FORM_DATA_CONTENT_TYPE);
                } else {
                    return Optional.absent();
                }
            }
        };
    }

    @Provides
    public EntityRequestBodyReaderFactory formDataRequestBodyReaderFactory() {
        return new EntityRequestBodyReaderFactory() {
            public Optional<FormDataEntityRequestBodyReader> mayBuildFor(Type valueType, String contentType) {
                if (!contentType.toLowerCase(Locale.ENGLISH).startsWith(FORM_DATA_CONTENT_TYPE)) {
                    return Optional.absent();
                }
                return Optional.of(new FormDataEntityRequestBodyReader());
            }
        };
    }
}
