package samplest.hello

import restx.RestxRequest
import restx.annotations.GET
import restx.annotations.Param
import restx.annotations.RestxResource
import restx.factory.Component

/**
 * Created by xhanin on 13/01/2018.
 */
@Component
@RestxResource
class HeaderResource {
    @GET("/headers")
    fun headers(@Param(kind = Param.Kind.CONTEXT) request: RestxRequest): Map<String, String> = request.headers
}