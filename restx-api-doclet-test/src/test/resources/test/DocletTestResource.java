package test;

import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;

@RestxResource @Component
public class DocletTestResource {
    /**
     * Test
     * @param param1 param number one
     * @param param2 param number two
     * @return my return value
     */
    @GET("/test/:param1")
    public String test(String param1, String param2) {
        return "test";
    }
}