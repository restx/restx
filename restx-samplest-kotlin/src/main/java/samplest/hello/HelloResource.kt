package samplest.hello

import restx.annotations.GET
import restx.annotations.RestxResource
import restx.factory.Component

/**
 * Created by xhanin on 13/01/2018.
 */
@Component @RestxResource
class HelloResource {
    @GET("/hello") fun hello(): String = "Hello restx kotlin!"
}