package samplest.cors;

import restx.annotations.*;
import restx.factory.Component;

import java.util.Map;

/**
 * Date: 28/12/13
 * Time: 10:17
 */
@Component @RestxResource
public class CORSSamplestResource {
    @GET("/cors/1")
    public String get1() {
        return "CORS1";
    }

    @HEAD("/cors/1")
    public void head1() {
    }

    @POST("/cors/1")
    public String post1(Map param) {
        return "CORS1";
    }

    @GET("/cors/2")
    public String get2() {
        return "CORS2";
    }

    @HEAD("/cors/2")
    public void head2() {
    }

    @POST("/cors/2")
    public String post2(Map param) {
        return "CORS2";
    }

    @PUT("/cors/2")
    public String put2(Map param) {
        return "CORS2";
    }

    @PUT("/cors/3")
    public String put3(Map param) {
        return "CORS3";
    }
}
