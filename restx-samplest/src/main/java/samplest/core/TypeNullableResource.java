package samplest.core;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;

@Component
@RestxResource
public class TypeNullableResource {
    @GET("/int-number")
    public int intNumberJava() {
        return 1;
    }

    @GET("/long-number")
    public long longNumberJava() {
        return 1L;
    }

    @GET("/double-number")
    public double doubleNumberJava() {
        return 1.0D;
    }

    @GET("/float-number")
    public float floatNumberJava() {
        return 1F;
    }

    @GET("/byte-number")
    public byte byteJava() {
        return 1;
    }

    @GET("/boolean")
    public boolean booleanJava() {
        return true;
    }
}
