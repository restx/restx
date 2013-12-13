package samplest.core;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Iterables;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Date: 13/12/13
 * Time: 23:06
 */
@RestxResource @Component
public class PolymorphicResource {
    @JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    public static class A {
        public String a;
    }
    public static class B extends A {
        public String b;
    }

    @GET("/polymorphic/single/{type}")
    public A bytype(String type) {
        if ("B".equalsIgnoreCase(type)) {
            B b = new B();
            b.b = "b";
            b.a = "a";
            return b;
        } else {
            A a = new A();
            a.a = "a";
            return a;
        }
    }

    @GET("/polymorphic/list/{type}")
    public Iterable<A> findAllByType(String type) {
        List<A> l = new ArrayList<>();
        A a = new A();
        a.a = "a1";
        l.add(a);
        if ("B".equalsIgnoreCase(type)) {
            B b = new B();
            b.b = "b";
            b.a = "a2";
            l.add(b);
        } else {
            a = new A();
            a.a = "a2";
            l.add(a);
        }
        return l;
    }

    @POST("/polymorphic")
    public A post(A a) {
        return a;
    }
}
