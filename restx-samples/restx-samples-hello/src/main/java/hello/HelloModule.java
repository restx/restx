package hello;

import com.google.common.base.Charsets;
import restx.SignatureKey;
import restx.factory.Module;
import restx.factory.Provides;

@Module
public class HelloModule {
    @Provides
    public SignatureKey signatureKey() {
         return new SignatureKey("this is the key for my restx app".getBytes(Charsets.UTF_8));
    }
}
