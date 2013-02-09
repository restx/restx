package restx.converters;

import com.google.common.base.CharMatcher;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import restx.factory.*;

/**
 * User: xavierhanin
 * Date: 2/5/13
 * Time: 11:30 PM
 */
public class DefaultConverters extends DefaultFactoryMachine {
    public DefaultConverters() {
        super(0,
                new NoDepsMachineEngine<StringConverter>(Name.of(StringConverter.class, "BooleanConverter"), BoundlessComponentBox.FACTORY) {
                    @Override
                    public StringConverter doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return new SimpleStringConverter<Boolean>(Boolean.class) {
                            @Override
                            public Boolean apply(String input) {
                                return Boolean.valueOf(input);
                            }
                        };
                    }
                }
                ,
                new NoDepsMachineEngine<StringConverter>(Name.of(StringConverter.class, "IntegerConverter"), BoundlessComponentBox.FACTORY) {
                    @Override
                    public StringConverter doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return new SimpleStringConverter<Integer>(Integer.class) {
                            @Override
                            public Integer apply(String input) {
                                return Integer.valueOf(input);
                            }
                        };
                    }
                }
                ,
                new NoDepsMachineEngine<StringConverter>(Name.of(StringConverter.class, "LongConverter"), BoundlessComponentBox.FACTORY) {
                    @Override
                    public StringConverter doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return new SimpleStringConverter<Long>(Long.class) {
                            @Override
                            public Long apply(String input) {
                                return Long.valueOf(input);
                            }
                        };
                    }
                }
                ,
                new NoDepsMachineEngine<StringConverter>(Name.of(StringConverter.class, "DateTimeConverter"), BoundlessComponentBox.FACTORY) {
                    @Override
                    public StringConverter doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return new SimpleStringConverter<DateTime>(DateTime.class) {
                            @Override
                            public DateTime apply(String input) {
                                return CharMatcher.DIGIT.matchesAllOf(input)
                                        ? new DateTime(Long.parseLong(input)) // only digits, it's a timestamp
                                        : DateTime.parse(input);
                            }
                        };
                    }
                }
                ,
                new NoDepsMachineEngine<StringConverter>(Name.of(StringConverter.class, "DateMidnightConverter"), BoundlessComponentBox.FACTORY) {
                    @Override
                    public StringConverter doNewComponent(SatisfiedBOM satisfiedBOM) {
                        return new SimpleStringConverter<DateMidnight>(DateMidnight.class) {
                            @Override
                            public DateMidnight apply(String input) {
                                return CharMatcher.DIGIT.matchesAllOf(input)
                                        ? new DateMidnight(Long.parseLong(input)) // only digits, it's a timestamp
                                        : DateMidnight.parse(input);
                            }
                        };
                    }
                }
        );
    }
}
