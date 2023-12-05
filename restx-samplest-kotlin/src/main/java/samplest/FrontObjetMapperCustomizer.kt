package samplest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import restx.factory.Component
import restx.factory.NamedComponent
import restx.factory.SingleComponentNameCustomizerEngine
import restx.jackson.FrontObjectMapperFactory

@Component
class FrontObjectMapperCustomizer :
    SingleComponentNameCustomizerEngine<ObjectMapper>(5, FrontObjectMapperFactory.NAME) {

    override fun customize(namedComponent: NamedComponent<ObjectMapper>): NamedComponent<ObjectMapper> {
        val objectMapper = namedComponent.component
        objectMapper.registerKotlinModule()
        return namedComponent
    }
}
