package samplest.core;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ObjectArrays;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @GET("/params/query/withOptionalString")
    public String queryparams(String a, Optional<String> b) {
        return "a=" + a + " b=" + b.or("default");
    }

    @GET("/params/query/withOptionalDate")
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

    @GET("/params/listStringParams")
    public List<String> listStringParams(List<String> params, List<String> otherParams) {
        return FluentIterable.from(Iterables.concat(params, otherParams)).toList();
    }

    @GET("/params/setStringParams")
    public Set<String> setStringParams(Set<String> params, Set<String> otherParams) {
        return FluentIterable.from(Iterables.concat(params, otherParams)).toSet();
    }

    @GET("/params/iterableStringParams")
    public Iterable<String> iterableStringParams(Iterable<String> params, Iterable<String> otherParams) {
        return Iterables.concat(params, otherParams);
    }

    @GET("/params/optionalIterableStringParams")
    public Iterable<String> optionalIterableStringParams(Optional<Iterable<String>> params, Optional<Iterable<String>> otherParams) {
        return Iterables.concat(params.or(Collections.<String>emptyList()), otherParams.or(Collections.<String>emptyList()));
    }

    @GET("/params/arrayedStringParams")
    public String[] arrayedStringParams(String[] params, String[] otherParams) {
        return ObjectArrays.concat(params, otherParams, String.class);
    }

    @GET("/params/optionalArrayedStringParams")
    public String[] optionalArrayedStringParams(Optional<String[]> params, Optional<String[]> otherParams) {
        return ObjectArrays.concat(params.or(new String[0]), otherParams.or(new String[0]), String.class);
    }

    @GET("/params/listJodaDatesParams")
    public List<DateTime> listJodaDatesParams(List<DateTime> params, List<DateTime> otherParams) {
        return FluentIterable.from(Iterables.concat(params, otherParams)).toList();
    }

    @GET("/params/setJodaDatesParams")
    public Set<DateTime> setJodaDatesParams(Set<DateTime> params, Set<DateTime> otherParams) {
        return FluentIterable.from(Iterables.concat(params, otherParams)).toSet();
    }

    @GET("/params/iterableJodaDatesParams")
    public Iterable<DateTime> iterableJodaDatesParams(Iterable<DateTime> params, Iterable<DateTime> otherParams) {
        return Iterables.concat(params, otherParams);
    }

    @GET("/params/optionalIterableJodaDatesParams")
    public Iterable<DateTime> optionalIterableJodaDatesParams(Optional<Iterable<DateTime>> params, Optional<Iterable<DateTime>> otherParams) {
        return Iterables.concat(params.or(Collections.<DateTime>emptyList()), otherParams.or(Collections.<DateTime>emptyList()));
    }

    @GET("/params/arrayedJodaDatesParams")
    public DateTime[] arrayedJodaDatesParams(DateTime[] params, DateTime[] otherParams) {
        return ObjectArrays.concat(params, otherParams, DateTime.class);
    }

    @GET("/params/optionalArrayedJodaDatesParams")
    public DateTime[] optionalArrayedJodaDatesParams(Optional<DateTime[]> params, Optional<DateTime[]> otherParams) {
        return ObjectArrays.concat(params.or(new DateTime[0]), otherParams.or(new DateTime[0]), DateTime.class);
    }
}
