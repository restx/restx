package samplest.hello.primitive

import restx.annotations.GET
import restx.annotations.RestxResource
import restx.factory.Component
import javax.inject.Named

@Component
@RestxResource
class PrimitiveResource(
    @Named("int") private val anInt: Int,
    @Named("intWhen") private val anIntWhen: Int,
    @Named("intAlternativeWhen") private val anIntAlternativeWhen: Int,
) {
    @GET("/primitive")
    fun primitive(): Int = anInt + anIntWhen + anIntAlternativeWhen
}
