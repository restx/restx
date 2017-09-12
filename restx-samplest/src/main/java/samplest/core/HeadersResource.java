package samplest.core;

import restx.annotations.*;
import restx.factory.Component;

@RestxResource("/headers") @Component
public class HeadersResource {

    @GET("/expires")
    @ExpiresAfter("2d 4h")
    public String expireHeader() {
        return "hello";
    }

    public static class Foo {
        String id;
        String name;

        public String getId() {
            return id;
        }

        Foo setId(String id) {
            this.id = id;
            return this;
        }

        public String getName() {
            return name;
        }

        public Foo setName(String name) {
            this.name = name;
            return this;
        }
    }

    @POST("/foos")
    @LocationHeader("{_currentUri_}/{id}")
    public Foo locationHeader(Foo foo) {
        foo.setId("123456");
        return foo;
    }

    @POST("/foos2")
    @LocationHeader("{_baseUri_}/headers/foos2/{id}")
    public Foo locationHeader2(Foo foo) {
        foo.setId("123456");
        return foo;
    }
}
