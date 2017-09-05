package restx.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;
import org.joda.time.format.PeriodParser;

import java.util.Locale;

public class MorePeriods {

    private static final PeriodParser PERIOD_PARSER = new PeriodFormatterBuilder()
                .appendYears().appendSuffix("y").appendSeparatorIfFieldsAfter(" ")
                .appendMonths().appendSuffix("mo").appendSeparatorIfFieldsAfter(" ")
                .appendWeeks().appendSuffix("w").appendSeparatorIfFieldsAfter(" ")
                .appendDays().appendSuffix("d").appendSeparatorIfFieldsAfter(" ")
                .appendHours().appendSuffix("h").appendSeparatorIfFieldsAfter(" ")
                .appendMinutes().appendSuffix("m").appendSeparatorIfFieldsAfter(" ")
                .appendSeconds().appendSuffix("s")
                .toParser();

    private static class ParseableDurationForLocale {
        String duration;
        Locale locale;

        public ParseableDurationForLocale(String duration, Locale locale) {
            this.duration = duration;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ParseableDurationForLocale that = (ParseableDurationForLocale) o;

            if (duration != null ? !duration.equals(that.duration) : that.duration != null) return false;
            return locale != null ? locale.equals(that.locale) : that.locale == null;
        }

        @Override
        public int hashCode() {
            int result = duration != null ? duration.hashCode() : 0;
            result = 31 * result + (locale != null ? locale.hashCode() : 0);
            return result;
        }
    }

    private static final LoadingCache<ParseableDurationForLocale, Period> PARSED_PERIODS_CACHE = CacheBuilder.newBuilder()
            .maximumSize(200)
            .build(new CacheLoader<ParseableDurationForLocale, Period>() {
                @Override
                public Period load(ParseableDurationForLocale parseableDurationForLocale) throws Exception {
                    MutablePeriod period = new MutablePeriod();
                    PERIOD_PARSER.parseInto(period, parseableDurationForLocale.duration, 0, parseableDurationForLocale.locale);
                    return period.toPeriod();
                }
            });

    /**
     * Parses a period from a string looking like "1y 2mo 1w 2d 1h 2m 10s"
     */
    public static Period parsePeriod(String duration, Locale currentLocale){
        return PARSED_PERIODS_CACHE.getUnchecked(new ParseableDurationForLocale(duration, currentLocale));
    }
}
