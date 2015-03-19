package samplest.core;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

/**
 * Date: 13/12/13
 * Time: 23:56
 */
@RestxResource @Component
@PermitAll
public class ParametersResource {
    public static class POJO {
        String val;

        public POJO() {
            this(null);
        }

        public POJO(String val) {
            this.val = val;
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }
    }

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

    @POST("/params/optionalPost")
    public POJO optionalPostContent(Optional<POJO> pojo) {
        return pojo.or(new POJO("empty"));
    }

    @POST("/params/mandatoryPost")
    public POJO postContent(POJO pojo) {
        return pojo;
    }
}
