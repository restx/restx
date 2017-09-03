package restx.http;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import org.joda.time.*;
import org.joda.time.format.PeriodFormatterBuilder;
import org.joda.time.format.PeriodParser;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.StdRoute;
import restx.annotations.ExpiresAfter;
import restx.description.OperationDescription;
import restx.description.ResourceDescription;
import restx.factory.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Component
public class ExpiresHeaderFilter extends EntityRelatedFilter {

    public ExpiresHeaderFilter() {
        super(Predicates.<StdRoute>alwaysTrue(), Predicates.<ResourceDescription>alwaysTrue(),
                new OperationDescription.Matcher().havingAnyAnnotations(ExpiresAfter.class)
        );
    }

    @Override
    protected void onEntityOutput(StdRoute stdRoute, RestxRequest req, RestxResponse resp,
                                  Optional<?> input, Optional<?> output,
                                  ResourceDescription resourceDescription, OperationDescription operationDescription) {

        if(!output.isPresent()) {
            return;
        }

        ExpiresAfter expiresAfterAnn = operationDescription.findAnnotation(ExpiresAfter.class).get();

        PeriodParser parser = new PeriodFormatterBuilder()
                .appendYears().appendSuffix("y").appendSeparatorIfFieldsAfter(" ")
                .appendMonths().appendSuffix("mo").appendSeparatorIfFieldsAfter(" ")
                .appendWeeks().appendSuffix("w").appendSeparatorIfFieldsAfter(" ")
                .appendDays().appendSuffix("d").appendSeparatorIfFieldsAfter(" ")
                .appendHours().appendSuffix("h").appendSeparatorIfFieldsAfter(" ")
                .appendMinutes().appendSuffix("m").appendSeparatorIfFieldsAfter(" ")
                .appendSeconds().appendSuffix("s")
                .toParser();

        MutablePeriod period = new MutablePeriod();
        parser.parseInto(period, expiresAfterAnn.value(), 0, Locale.US);
        String expiresHeaderValue = createRFC1123DateFormat().format(DateTime.now().plus(period).toDate());

        resp.setHeader("Expires", expiresHeaderValue);
    }

    public static DateFormat createRFC1123DateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(DateTimeZone.UTC.toTimeZone());
        return dateFormat;
    }
}
