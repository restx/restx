package samplest.core;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;

/**
 * Date: 13/12/13
 * Time: 23:56
 */
@RestxResource @Component
public class ParametersResource {
    @GET("/params/path/{a}/:b/{c:\\d+}:d")
    public String pathparams(String a, String b, String c, String d) {
        return "a=" + a + " b=" + b + " c=" + c + " d=" + d;
    }

    @GET("/params/query/1")
    public String queryparams(String a, Optional<String> b) {
        return "a=" + a + " b=" + b.or("default");
    }

    @GET("/params/query/2")
    public String queryparams2(DateTime a, Optional<DateTime> b) {
        return "a=" + a + " b=" + b.or(new DateTime(0L, DateTimeZone.UTC));
    }
}
