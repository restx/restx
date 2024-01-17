package samplest.hello.primitive

import restx.factory.Alternative
import restx.factory.Module
import restx.factory.Provides
import restx.factory.When
import javax.inject.Named

@Module
class PrimitiveModule {
    @Provides
    @Named("int")
    fun integer(): Int = 1

    @Provides
    @Named("intWhen")
    @When(name = "restx.mode", value = "test")
    fun integerWhen(): Int = 2

    @Provides
    @Named("intWhen")
    @When(name = "restx.mode", value = "dev")
    fun integerWhenDev(): Int = 2

    @Provides
    @Named("intAlternativeWhen")
    fun integerAlternativeWhen(): Int = 1

    @Alternative(to = Int::class, named = "intAlternativeWhen")
    @When(name = "restx.mode", value = "test")
    fun integerAlternativeWhenAlt(): Int = 3
}
