package samplest.jacksonviews;

import com.google.common.collect.Lists;
import restx.annotations.GET;
import restx.annotations.Produces;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;
import samplest.domain.Car;

import java.util.List;

/**
 * User: eoriou
 * Date: 04/12/2013
 * Time: 11:15
 */

@RestxResource
@Component
public class JacksonViewsResource {

    @GET("/jacksonviews/cars")
    @Produces("application/json;view=samplest.jacksonviews.Views$Frontal$Details")
    @PermitAll
    public List<Car> getCars() {

        return Lists.newArrayList(
                new Car().setBrand("Brand1").setModel("Model1"),
                new Car().setBrand("Brand1").setModel("Model2"),
                new Car().setBrand("Brand2").setModel("Model1"),
                new Car().setBrand("Brand3").setModel("Model1")
        );
    }
}
