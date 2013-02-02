package restx.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * User: xavierhanin
 * Date: 2/2/13
 * Time: 5:52 PM
 */
public class FixedPrecisionDeserializer extends JsonDeserializer<BigDecimal> {
    private final int precision;
    private final BigDecimal divisor;

    public FixedPrecisionDeserializer(int precision) {
        this.precision = precision;
        divisor = new BigDecimal(10).pow(precision);
    }

    @Override
    public BigDecimal deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return new BigDecimal(jp.getLongValue()).divide(divisor);
    }
}
