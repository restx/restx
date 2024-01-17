package samplest.hello.primitive

import org.assertj.core.api.Assertions
import org.junit.ClassRule
import org.junit.Test
import restx.tests.RestxServerRule

class PrimitiveResourceTest {
    @Test
    fun should_accept_kotlin_nullable_in_quereyParams() {
        val httpRequest = Companion.server
            .client()
            .authenticatedAs("admin")
            .GET("/api/primitive")
        Assertions.assertThat(httpRequest.code()).isEqualTo(200)
        Assertions.assertThat(httpRequest.body()).isEqualTo("6")
    }

    companion object {
        @JvmField
        @ClassRule
        var server = RestxServerRule()
    }
}
