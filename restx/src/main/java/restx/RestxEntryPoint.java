package restx;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 8:59 PM
 */
public interface RestxEntryPoint {
    RestxContext.Definition getCtxDefinition();

    ObjectMapper getObjectMapper();

    SignatureKey getSignatureKey();

    OnStartup getOnStartup();
}
