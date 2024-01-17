package samplest.hello

import restx.annotations.GET
import restx.annotations.RestxResource
import restx.factory.Component

@Component
@RestxResource("/factory")
class FactoryInjectionResource(
    private val testSetInjection: Set<TestSetInjection>,
    private val testSetAbstract: Set<TestSetAbstract>
) {
    @GET("/interface/set")
    fun testInterfaceSet(): Set<String> = testSetInjection.map { it.javaClass.name }.toSet()

    @GET("/abstract/set")
    fun testAbstractSet(): Set<String> = testSetAbstract.map { it.javaClass.name }.toSet()
}

interface TestSetInjection

@Component
class TestSetInjectionImpl01 : TestSetInjection

@Component
class TestSetInjectionImpl02 : TestSetInjection

abstract class TestSetAbstract

@Component
class TestSetAbstractImpl01 : TestSetAbstract()

@Component
class TestSetAbstractImpl02 : TestSetAbstract()