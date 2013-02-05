package restx.converters;

import com.google.common.base.CharMatcher;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import restx.factory.DefaultFactoryMachine;
import restx.factory.Factory;
import restx.factory.Name;

/**
 * User: xavierhanin
 * Date: 2/5/13
 * Time: 11:30 PM
 */
public class DefaultConverters extends DefaultFactoryMachine {
    public DefaultConverters() {
        super(0,
                SingleComponentBoxSupplier.boundless(Name.of(StringConverter.class, "BooleanConverter"), new SingleComponentSupplier<StringConverter>() {
                    @Override
                    public StringConverter<Boolean> newComponent(Factory factory) {
                        return new SimpleStringConverter<Boolean>(Boolean.class) {
                            @Override
                            public Boolean apply(String input) {
                                return Boolean.valueOf(input);
                            }
                        };
                    }
                })
                ,
                SingleComponentBoxSupplier.boundless(Name.of(StringConverter.class, "IntegerConverter"), new SingleComponentSupplier<StringConverter>() {
                    @Override
                    public StringConverter<Integer> newComponent(Factory factory) {
                        return new SimpleStringConverter<Integer>(Integer.class) {
                            @Override
                            public Integer apply(String input) {
                                return Integer.valueOf(input);
                            }
                        };
                    }
                })
                ,
                SingleComponentBoxSupplier.boundless(Name.of(StringConverter.class, "LongConverter"), new SingleComponentSupplier<StringConverter>() {
                    @Override
                    public StringConverter<Long> newComponent(Factory factory) {
                        return new SimpleStringConverter<Long>(Long.class) {
                            @Override
                            public Long apply(String input) {
                                return Long.valueOf(input);
                            }
                        };
                    }
                })
                ,
                SingleComponentBoxSupplier.boundless(Name.of(StringConverter.class, "DateTimeConverter"), new SingleComponentSupplier<StringConverter>() {
                    @Override
                    public StringConverter<DateTime> newComponent(Factory factory) {
                        return new SimpleStringConverter<DateTime>(DateTime.class) {
                            @Override
                            public DateTime apply(String input) {
                                return CharMatcher.DIGIT.matchesAllOf(input)
                                        ? new DateTime(Long.parseLong(input)) // only digits, it's a timestamp
                                        : DateTime.parse(input);
                            }
                        };
                    }
                })
                ,
                SingleComponentBoxSupplier.boundless(Name.of(StringConverter.class, "DateMidnightConverter"), new SingleComponentSupplier<StringConverter>() {
                    @Override
                    public StringConverter<DateMidnight> newComponent(Factory factory) {
                        return new SimpleStringConverter<DateMidnight>(DateMidnight.class) {
                            @Override
                            public DateMidnight apply(String input) {
                                return CharMatcher.DIGIT.matchesAllOf(input)
                                        ? new DateMidnight(Long.parseLong(input)) // only digits, it's a timestamp
                                        : DateMidnight.parse(input);
                            }
                        };
                    }
                })
        );
    }
}
