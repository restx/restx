package restx.types;

import java.time.*;

public class JDK8RawTypesDefinition extends RawTypesDefinition.ClassBasedRawTypesDefinition {
    public JDK8RawTypesDefinition() {
        super(
                Instant.class, DayOfWeek.class, LocalDate.class, LocalDateTime.class, LocalTime.class,
                Month.class, Year.class, ZoneId.class
        );
    }
}
