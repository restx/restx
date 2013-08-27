package restx.converters;

import com.google.common.base.CharMatcher;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import restx.factory.*;

import javax.inject.Named;

/**
 * User: xavierhanin
 * Date: 2/5/13
 * Time: 11:30 PM
 */
@Module
public class DefaultConverters {
    @Provides @Named("BooleanConverter")
    public StringConverter booleanConverter() {
        return new SimpleStringConverter<Boolean>(Boolean.class) {
            @Override
            public Boolean apply(String input) {
                return Boolean.valueOf(input);
            }
        };
    }
    @Provides @Named("IntegerConverter")
    public StringConverter integerConverter() {
        return new SimpleStringConverter<Integer>(Integer.class) {
            @Override
            public Integer apply(String input) {
                return Integer.valueOf(input);
            }
        };
    }
    @Provides @Named("LongConverter")
    public StringConverter longConverter() {
        return new SimpleStringConverter<Long>(Long.class) {
            @Override
            public Long apply(String input) {
                return Long.valueOf(input);
            }
        };
    }
    @Provides @Named("DateTimeConverter")
    public StringConverter dateTimeConverter() {
        return new SimpleStringConverter<DateTime>(DateTime.class) {
            @Override
            public DateTime apply(String input) {
                return CharMatcher.DIGIT.matchesAllOf(input)
                        ? new DateTime(Long.parseLong(input)) // only digits, it's a timestamp
                        : DateTime.parse(input);
            }
        };
    }
    @Provides @Named("DateMidnightConverter")
    public StringConverter dateMidmnightConverter() {
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
