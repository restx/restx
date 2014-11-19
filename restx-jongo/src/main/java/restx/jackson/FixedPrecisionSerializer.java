package restx.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.math.BigDecimal;

/**
 * User: xavierhanin
 * Date: 2/2/13
 * Time: 5:51 PM
 */
public class FixedPrecisionSerializer extends StdSerializer<BigDecimal> implements ContextualSerializer {
    private final int precision;
    private final BigDecimal mul;

    public FixedPrecisionSerializer() {
        this(0);
    }

    public FixedPrecisionSerializer(int precision) {
        super(BigDecimal.class);
        this.precision = precision;
        mul = new BigDecimal(10).pow(precision);
    }

    @Override
    public void serialize(BigDecimal value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeNumber(value.multiply(mul).longValue());
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Member member = property.getMember().getMember();
        if (member instanceof Field) {
            FixedPrecision fixedPrecision = ((AnnotatedElement) member).getAnnotation(FixedPrecision.class);
            if (fixedPrecision != null) {
                return new FixedPrecisionSerializer(fixedPrecision.value());
            }
        }
        return this;
    }
}
