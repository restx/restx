package samplest.hello

import restx.annotations.GET
import restx.annotations.QueryParam
import restx.annotations.RestxResource
import restx.factory.Component
import java.util.*

@Component
@RestxResource
class TypeNullableResource {
    @GET("/int-number-kt")
    fun intNumberKotlin(): Int = 1

    @GET("/long-number-kt")
    fun longNumberKotlin(): Long = 1L

    @GET("/double-number-kt")
    fun doubleNumberKotlin(): Double = 1.0

    @GET("/float-number-kt")
    fun floatNumberKotlin(): Float = 1F

    @GET("/byte-number-kt")
    fun byteKotlin(): Byte = 1

    @GET("/boolean-kt")
    fun booleanKotlin(): Boolean = true

    @GET("/string-nullable-kt")
    fun stringNullableKotlin(): String? = null

    @GET("/string-non-null-kt")
    fun stringNonNullKotlin(): String = "coucou"

    @GET("/find-query-nullable-kotlin")
    fun query(
        @QueryParam nullable: String?,
        @QueryParam optionalJ8: Optional<String>,
        @QueryParam optionalG: com.google.common.base.Optional<String>,
        @QueryParam nonNull: String,
    ): String? = null

    @GET("/find-nullable-criteria-kotlin")
    fun queryNullableCriteria(
        criteria: Criteria?
    ): Criteria? = criteria

    @GET("/find-criteria-kotlin")
    fun queryCriteria(
        criteria: Criteria
    ): Criteria = criteria
}

class Criteria(val nonNull: String)
