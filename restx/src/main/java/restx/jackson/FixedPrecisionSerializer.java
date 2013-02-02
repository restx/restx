package restx.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * User: xavierhanin
 * Date: 2/2/13
 * Time: 5:51 PM
 */
public class FixedPrecisionSerializer extends JsonSerializer<BigDecimal> {
    private final int precision;
    private final BigDecimal mul;

    public FixedPrecisionSerializer(int precision) {
        this.precision = precision;
        mul = new BigDecimal(10).pow(precision);
    }

    @Override
    public void serialize(BigDecimal value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeNumber(value.multiply(mul).longValue());
    }
}
