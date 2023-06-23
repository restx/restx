package samplest.jsr310;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import java.time.Clock;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RestxResource
@Component
public class Jsr310Resource {

    final Clock clock;
    public Jsr310Resource(Clock clock) {
        this.clock = clock;
    }

    @GET("/time")
    @PermitAll
    public String time() {
        return LocalTime.now(clock).format(DateTimeFormatter.ISO_LOCAL_TIME);
    }
}
