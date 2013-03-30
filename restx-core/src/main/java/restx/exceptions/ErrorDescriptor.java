package restx.exceptions;

import com.google.common.collect.ImmutableMap;

/**
 * User: xavierhanin
 * Date: 3/19/13
 * Time: 10:25 PM
 */
public class ErrorDescriptor {
    private final int errorStatus;
    private final String errorCode;
    private final String description;
    private final ImmutableMap<String, ErrorFieldDescriptor> fields;

    public ErrorDescriptor(int errorStatus, String errorCode, String description,
                           ImmutableMap<String, ErrorFieldDescriptor> fields) {
        this.errorStatus = errorStatus;
        this.errorCode = errorCode;
        this.description = description;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "ErrorDescriptor{" +
                "errorStatus=" + errorStatus +
                ", errorCode='" + errorCode + '\'' +
                ", description='" + description + '\'' +
                ", fields=" + fields +
                '}';
    }

    public int getErrorStatus() {
        return errorStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDescription() {
        return description;
    }

    public ImmutableMap<String, ErrorFieldDescriptor> getFields() {
        return fields;
    }

    public static class ErrorFieldDescriptor {
        private final String field;
        private final String description;

        public ErrorFieldDescriptor(String field, String description) {
            this.field = field;
            this.description = description;
        }

        public String getField() {
            return field;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "ErrorFieldDescriptor{" +
                    "field='" + field + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

}
