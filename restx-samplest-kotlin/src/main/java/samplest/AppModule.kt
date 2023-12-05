package samplest

import restx.factory.Module
import restx.factory.Provides
import javax.inject.Named

/**
 * Created by xhanin on 13/01/2018.
 */
@Module
class AppModule {
    @Provides @Named("restx.app.package")
    fun appPackage(): String = "samplest"
}


