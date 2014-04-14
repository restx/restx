package restx.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import restx.jackson.FixedPrecision;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * User: xavierhanin
 * Date: 2/2/13
 * Time: 5:52 PM
 */
public class FixedPrecisionDeserializer extends StdDeserializer<BigDecimal> implements ContextualDeserializer {
    private final int precision;
    private final BigDecimal divisor;

    public FixedPrecisionDeserializer() {
        this(0);
    }

    public FixedPrecisionDeserializer(int precision) {
        super(BigDecimal.class);
        this.precision = precision;
        divisor = new BigDecimal(10).pow(precision);
    }

    @Override
    public BigDecimal deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return new BigDecimal(jp.getLongValue()).divide(divisor);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        FixedPrecision fixedPrecision = ((Field) property.getMember().getMember()).getAnnotation(FixedPrecision.class);
        if (fixedPrecision != null) {
            return new FixedPrecisionDeserializer(fixedPrecision.value());
        }
        return this;
    }
}
