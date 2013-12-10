package restx.exceptions;

import restx.common.UUIDGenerator;
import restx.factory.Component;
import restx.http.HttpStatus;

import javax.inject.Named;

/**
 * Date: 10/12/13
 * Time: 20:52
 */
@Component
public class RestxErrors {
    private final UUIDGenerator uuidGenerator;

    public RestxErrors() {
        this.uuidGenerator = ExceptionsFactory.currentUUIDGenerator();
    }

    public <E> RestxError<E> on(Class<E> errorCode) {
        ErrorCode code = errorCode.getAnnotation(ErrorCode.class);
        HttpStatus errorStatus = code != null ? code.status() : HttpStatus.BAD_REQUEST;
        String error = code != null ? code.code() : errorCode.getSimpleName();
        String description = code != null ? code.description() : errorCode.getName();
        return new RestxError<>(uuidGenerator.doGenerate(), errorStatus, error, description);
    }
}
