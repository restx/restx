package restx.tests.json;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.base.Optional;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
* Date: 3/2/14
* Time: 21:35
*/
public class JsonWithLocationsParser {
    public ParsedJsonWithLocations parse(File file, Charset cs, Class type) throws IOException {
        return parse(new FileJsonSource(file, cs), type);
    }

    public ParsedJsonWithLocations parse(URL url, Charset cs, Class type) throws IOException {
        return parse(new URLJsonSource(url, cs), type);
    }

    public ParsedJsonWithLocations parse(String content, Class type) throws IOException {
        return parse(new StringJsonSource("", content), type);
    }

    public ParsedJsonWithLocations parse(JsonSource source, Class type) throws IOException {
        String content = source.content();
        ParsedJsonLocations locations = new ParsedJsonLocations(content);
        Object o = reader(locations, type).readValue(content);
        return new ParsedJsonWithLocations(locations, o);
    }

    protected ObjectReader reader(final ParsedJsonLocations locations, Class type) {
        SimpleModule module = new SimpleModule().setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                return new ContextualLocationDeserializerWrapper(locations,
                        super.modifyDeserializer(config, beanDesc, deserializer));
            }

            @Override
            public JsonDeserializer<?> modifyArrayDeserializer(DeserializationConfig config, ArrayType valueType, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                return new ContextualLocationDeserializerWrapper(locations,
                        super.modifyArrayDeserializer(config, valueType, beanDesc, deserializer));
            }

            @Override
            public JsonDeserializer<?> modifyMapDeserializer(DeserializationConfig config, MapType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                return new ContextualLocationDeserializerWrapper(locations,
                        super.modifyMapDeserializer(config, type, beanDesc, deserializer));
            }
        });
        return new ObjectMapper().registerModule(module).reader(type);
    }

    public static class ParsedJsonLocations {
        private final String source;
        private Map<Object, JsonObjectLocation> locations = new HashMap<>();

        public ParsedJsonLocations(String source) {
            this.source = source;
        }

        private void addLocation(Object o, JsonLocation from, JsonLocation to) {
            locations.put(objectKey(o), new JsonObjectLocation(source, from, to));
        }

        protected String objectKey(Object o) {
            return System.identityHashCode(o) + "-" + o;
        }

        public Optional<JsonObjectLocation> getLocationOf(Object o) {
            return Optional.fromNullable(locations.get(objectKey(o)));
        }

        @Override
        public String toString() {
            return "ParsedJsonLocations{" +
                    "locations=" + locations +
                    '}';
        }
    }

    public static class ParsedJsonWithLocations {
        private final ParsedJsonLocations locations;
        private final Object root;

        public ParsedJsonWithLocations(ParsedJsonLocations locations, Object root) {
            this.locations = locations;
            this.root = root;
        }

        public ParsedJsonLocations getLocations() {
            return locations;
        }

        public Object getRoot() {
            return root;
        }

        @Override
        public String toString() {
            return "ParsedJsonWithLocations{" +
                    "locations=" + locations +
                    ", root=" + root +
                    '}';
        }

    }



    private static class ContextualLocationDeserializerWrapper extends LocationDeserializerWrapper
            implements ContextualDeserializer, ResolvableDeserializer {
        private final ContextualDeserializer contextualDeserializer;

        private ContextualLocationDeserializerWrapper(ParsedJsonLocations locations, JsonDeserializer deserializer) {
            super(locations, deserializer);
            if (deserializer instanceof ContextualDeserializer) {
                contextualDeserializer = (ContextualDeserializer) deserializer;
            } else {
                contextualDeserializer = null;
            }
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            if (contextualDeserializer != null) {
                return new LocationDeserializerWrapper(locations, contextualDeserializer.createContextual(ctxt, property));
            }
            return this;
        }
    }

    private static class LocationDeserializerWrapper extends JsonDeserializer implements ResolvableDeserializer {
        protected final ParsedJsonLocations locations;
        private final JsonDeserializer deserializer;
        private final ResolvableDeserializer resolvableDeserializer;

        private LocationDeserializerWrapper(ParsedJsonLocations locations, JsonDeserializer deserializer) {
            this.locations = locations;
            this.deserializer = deserializer;
            if (deserializer instanceof ResolvableDeserializer) {
                resolvableDeserializer = (ResolvableDeserializer) deserializer;
            } else {
                resolvableDeserializer = null;
            }
        }

        @Override
        public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonLocation currentLocation = jp.getCurrentLocation();
            Object o = deserializer.deserialize(jp, ctxt);
            locations.addLocation(o, currentLocation, jp.getCurrentLocation());
            return o;
        }

        @Override
        public void resolve(DeserializationContext ctxt) throws JsonMappingException {
            if (resolvableDeserializer != null) {
                resolvableDeserializer.resolve(ctxt);
            }
        }
    }

}
