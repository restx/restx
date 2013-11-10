package restx.exceptions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import restx.http.HttpStatus;

import java.util.Map;

/**
 * User: xavierhanin
 * Date: 3/19/13
 * Time: 12:10 PM
 */
public class RestxError<T> {

    public static <E> RestxError<E> on(Class<E> errorCode) {
        ErrorCode code = errorCode.getAnnotation(ErrorCode.class);
        HttpStatus errorStatus = code != null ? code.status() : HttpStatus.BAD_REQUEST;
        String error = code != null ? code.code() : errorCode.getSimpleName();
        String description = code != null ? code.description() : errorCode.getName();
        return new RestxError<>(errorStatus, error, description);
    }

    private final HttpStatus errorStatus;
    private final String error;
    private final String description;
    private final Map<String, String> data = Maps.newLinkedHashMap();

    private RestxError(HttpStatus errorStatus, String error, String description) {
        this.errorStatus = errorStatus;
        this.error = error;
        this.description = description;
    }

    public RestxError<T> set(T field, String value) {
        if (value != null) {
            data.put(field.toString(), value);
        }
        return this;
    }

    public RestxException raise() {
        return new RestxException(
                ExceptionsFactory.currentUUIDGenerator().doGenerate(),
                DateTime.now().toDateTime(DateTimeZone.UTC),
                errorStatus, error,
                description,
                ImmutableMap.copyOf(data));
    }

    public static class RestxException  extends RuntimeException {
        private final String id;
        private final DateTime errorTime;
        private final HttpStatus errorStatus;
        private final String error;
        private final String description;
        private final ImmutableMap<String, String> data;

        RestxException(String id, DateTime errorTime, HttpStatus errorStatus, String error, String description, ImmutableMap<String, String> data) {
            super(String.format("[%s] [%s] [%3d~%s] %s - %s", errorTime, id, errorStatus.getCode(), error, description, data));
            this.id = id;
            this.errorTime = errorTime;
            this.errorStatus = errorStatus;
            this.error = error;
            this.description = description;
            this.data = data;
        }

        public String getId() {
            return id;
        }

        public DateTime getErrorTime() {
            return errorTime;
        }

        public HttpStatus getErrorStatus() {
            return errorStatus;
        }

        public String getError() {
            return error;
        }

        public String getDescription() {
            return description;
        }

        public ImmutableMap<String, String> getData() {
            return data;
        }

        public String toJSON() {
            StringBuilder sb = new StringBuilder().append("{")
                    .append("\"id\": \"").append(id).append("\",")
                    .append("\"errorTime\": \"").append(errorTime).append("\",")
                    .append("\"errorCode\": \"").append(error).append("\",")
                    .append("\"description\": \"").append(description.replace("\"", "\\\"")).append("\",")
                    ;

            sb.append("\"data\": {");
            if (!data.isEmpty()) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    sb.append("\"" + entry.getKey() + "\": \"").append(entry.getValue().replace("\"", "\\\"")).append("\",");
                }
                sb.setLength(sb.length() - 1);
            }
            sb.append("}");

            sb.append("}");
            return sb.toString();
        }
    }
}
