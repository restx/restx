package restx.converters;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import restx.factory.*;

/**
 * User: xavierhanin
 * Date: 2/5/13
 * Time: 11:18 PM
 */
public class MainStringConverter {
    private final ImmutableMap<Class, StringConverter> converters;

    public MainStringConverter(Iterable<StringConverter> stringConverters) {
        ImmutableMap.Builder<Class, StringConverter> builder = ImmutableMap.builder();
        for (StringConverter converter : stringConverters) {
            builder.put(Primitives.wrap(converter.getConvertedClass()), converter);
        }

        converters = builder.build();
    }

    public <T> T convert(String value, Class<T> toClass) {
        StringConverter converter = converters.get(Primitives.wrap(toClass));
        if (converter == null) {
            throw new IllegalArgumentException(String.format(
                    "no converter registered for %s. Converters are registered for: %s",
                    toClass.getName(), converters.keySet()));
        }
        return (T) converter.apply(value);
    }

    public static final class Machine extends SingleNameFactoryMachine<MainStringConverter> {
        public static final Name<MainStringConverter> NAME = Name.of(MainStringConverter.class, "MainStringConverter");

        public Machine() {
            super(0, new StdMachineEngine<MainStringConverter>(NAME, BoundlessComponentBox.FACTORY) {
                private Factory.Query<StringConverter> stringConverters = Factory.Query.byClass(StringConverter.class);

                @Override
                public MainStringConverter doNewComponent(SatisfiedBOM satisfiedBOM) {
                    return new MainStringConverter(satisfiedBOM.getAsComponents(stringConverters));
                }

                @Override
                public BillOfMaterials getBillOfMaterial() {
                    return BillOfMaterials.of(stringConverters);
                }
            });
        }
    }
}
