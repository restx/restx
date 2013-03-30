package restx;

import com.google.common.collect.ImmutableMap;

/**
 * User: xavierhanin
 * Date: 2/13/13
 * Time: 11:34 PM
 */
public final class Status {
    public static final ImmutableMap DELETED = ImmutableMap.of("status", "deleted");

    public static Status of(String status) {
        return new Status(status);
    }

    private String status;

    public Status() {
    }

    public Status(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
