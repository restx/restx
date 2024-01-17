package samplest.models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

@JsonDeserialize(using = MyCustomValueClass.Deserializer.class)
@JsonSerialize(using = MyCustomValueClass.Serializer.class)
public final class MyCustomValueClass<T> {
    String value;

    public MyCustomValueClass(String value) {
        this.value = value;
    }

    public T getFoo() {
        return null;
    }

    public static class Deserializer extends StdDeserializer<MyCustomValueClass> {
        public Deserializer() {
            super(MyCustomValueClass.class);
        }

        @Override
        public MyCustomValueClass deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return new MyCustomValueClass(jsonParser.getValueAsString());
        }
    }

    public static class Serializer extends StdSerializer<MyCustomValueClass> {
        public Serializer() {
            super(MyCustomValueClass.class);
        }

        @Override
        public void serialize(MyCustomValueClass myCustomValueClass, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(myCustomValueClass.value);
        }
    }
}
